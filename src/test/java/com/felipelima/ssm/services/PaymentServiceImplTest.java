package com.felipelima.ssm.services;

import com.felipelima.ssm.domain.Payment;
import com.felipelima.ssm.domain.PaymentEvent;
import com.felipelima.ssm.domain.PaymentState;
import com.felipelima.ssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void test_preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println(savedPayment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println(sm.getState().getId());

        System.out.println(preAuthedPayment);
    }

    @Transactional
    @RepeatedTest(10)
    void test_auth() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println(savedPayment);

        StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println(preAuthSM.getState().getId());

        System.out.println(preAuthedPayment);

        if (preAuthSM.getState().getId() == PaymentState.PRE_AUTH) {
            System.out.println("Payment is Pre Authorized");

            StateMachine<PaymentState, PaymentEvent> authSM = paymentService.authorizePayment(savedPayment.getId());

            Payment authedPayment = paymentRepository.getOne(savedPayment.getId());

            System.out.println("Result of Auth: " + authSM.getState().getId());

            System.out.println(authedPayment);
        } else {
            System.out.println("Payment failed pre-auth...");
        }
    }
}