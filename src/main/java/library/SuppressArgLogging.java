package library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link UseCase}-annotated class whose method arguments must not be logged.
 *
 * <p>Apply this annotation to any use case that handles sensitive input (e.g. credentials)
 * to prevent {@link UseCaseLoggingAdvice} from logging raw argument values. The advice will
 * substitute {@code [SUPPRESSED]} in place of the actual arguments.</p>
 *
 * <p>This is a defence-in-depth guard: the primary protection for credential-handling use
 * cases is to annotate them with {@code @Service} instead of {@code @UseCase} so that
 * the logging advice does not apply at all. This annotation covers the case where a future
 * developer accidentally uses {@code @UseCase} on a sensitive class.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressArgLogging {
}
