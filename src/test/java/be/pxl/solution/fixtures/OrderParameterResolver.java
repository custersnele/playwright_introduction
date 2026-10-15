package be.pxl.solution.fixtures;

import be.pxl.solution.data.Order;
import be.pxl.solution.data.OrderFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class OrderParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
		return parameter.isAnnotated(WithOrder.class) && parameter.getParameter().getType() == Order.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
		return OrderFactory.create(parameter.findAnnotation(WithOrder.class).orElseThrow().value());
	}
}
