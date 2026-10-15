package be.pxl.solution.fixtures;

import be.pxl.solution.data.OrderType;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom fixture: a test method parameter {@code @WithOrder(VIP) Order order} receives that order. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OrderParameterResolver.class)
public @interface WithOrder {
	OrderType value() default OrderType.STANDARD;
}
