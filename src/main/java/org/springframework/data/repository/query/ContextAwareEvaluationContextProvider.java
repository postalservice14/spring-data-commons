package org.springframework.data.repository.query;

import reactor.util.context.Context;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.repository.query.spi.EvaluationContextExtension;
import org.springframework.data.repository.query.spi.SubscriberContextAwareExtension;
import org.springframework.data.support.SubscriberContextAware;
import org.springframework.data.util.Lazy;

/**
 * {@link ExtensionAwareEvaluationContextProvider} implementation that is notified with a Reactor subscriber
 * {@link Context} upon execution. It propagates the context to context-aware {@link SubscriberContextAwareExtension
 * extensions}.
 *
 * @author Mark Paluch
 * @since 2.1
 */
public class ContextAwareEvaluationContextProvider extends ExtensionAwareEvaluationContextProvider
		implements SubscriberContextAware<ContextAwareEvaluationContextProvider> {

	/**
	 * Creates a new {@link ExtensionAwareEvaluationContextProvider}. Extensions are being looked up lazily from the
	 * {@link org.springframework.beans.factory.BeanFactory} configured.
	 */
	public ContextAwareEvaluationContextProvider() {}

	/**
	 * Creates a new {@link ExtensionAwareEvaluationContextProvider} for the given {@link EvaluationContextExtension}s.
	 *
	 * @param extensions must not be {@literal null}.
	 */
	public ContextAwareEvaluationContextProvider(List<? extends EvaluationContextExtension> extensions) {
		super(extensions);
	}

	private ContextAwareEvaluationContextProvider(ExtensionAwareEvaluationContextProvider parent) {
		super(parent);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.support.SubscriberContextAware#withSubscriberContext(reactor.util.context.Context)
	 */
	@Override
	public ContextAwareEvaluationContextProvider withSubscriberContext(Context context) {

		Lazy<List<EvaluationContextExtension>> extensions = Lazy.of(() -> {

			return getExtensions().stream().map(it -> {

				if (it instanceof SubscriberContextAwareExtension) {
					return ((SubscriberContextAwareExtension) it).withSubscriberContext(context);
				}

				return it;
			}).collect(Collectors.toList());
		});

		return new ContextAwareEvaluationContextProvider(this) {

			/* 
			 * (non-Javadoc)
			 * @see org.springframework.data.repository.query.ExtensionAwareEvaluationContextProvider#getExtensions()
			 */
			@Override
			protected List<? extends EvaluationContextExtension> getExtensions() {
				return extensions.get();
			}
		};
	}
}
