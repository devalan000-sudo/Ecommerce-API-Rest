package com.ecommerce.api.service.impl;

import com.ecommerce.api.entity.Order;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public String createCheckoutSession(Order order) {
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/cart?cancelled=true")
                    .putMetadata("order_id", order.getId().toString());

            for (var item : order.getItems()) {
                SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("mxn")
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProduct().getName())
                                        .build())
                                .setUnitAmount(item.getPrice().multiply(new BigDecimal("100")).longValue())
                                .build())
                        .setQuantity((long) item.getQuantity())
                        .build();
                paramsBuilder.addLineItem(lineItem);
            }

            SessionCreateParams params = paramsBuilder.build();

            Session session = Session.create(params);

            order.setStripeSessionId(session.getId());
            orderRepository.save(order);

            log.info("Checkout session creada para orden {}. URL: {}", order.getId(), session.getUrl());

            return session.getUrl();

        } catch (StripeException e) {
            log.error("Error al crear checkout session: {}", e.getMessage());
            throw new RuntimeException("Error al crear la sesión de pago: " + e.getMessage());
        }
    }

    @Override
    public void handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;
                default:
                    log.info("Evento de Stripe no manejado: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Error verificando firma del webhook: {}", e.getMessage());
            throw new RuntimeException("Error verificando webhook");
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            String orderId = session.getMetadata().get("order_id");
            if (orderId != null) {
                orderRepository.findById(Long.parseLong(orderId)).ifPresent(order -> {
                    order.setPaymentStatus(Order.PaymentStatus.PAID);
                    orderRepository.save(order);
                    log.info("Orden {} marcada como pagada", orderId);
                });
            }
        }
    }

    private void handlePaymentFailed(Event event) {
        log.info("Pago fallido: {}", event.getId());
    }
}
