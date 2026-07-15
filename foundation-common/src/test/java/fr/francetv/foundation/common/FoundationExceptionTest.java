package fr.francetv.foundation.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoundationExceptionTest {

    @Test
    void foundationExceptionShouldBeRuntimeException() {
        assertThat(new FoundationException("msg")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void technicalExceptionShouldExtendFoundationException() {
        assertThat(new FoundationTechnicalException("msg")).isInstanceOf(FoundationException.class);
    }

    @Test
    void businessExceptionShouldExtendFoundationException() {
        assertThat(new FoundationBusinessException("msg")).isInstanceOf(FoundationException.class);
    }

    @Test
    void shouldPreserveMessage() {
        assertThat(new FoundationTechnicalException("db unavailable").getMessage())
                .isEqualTo("db unavailable");
    }

    @Test
    void shouldPreserveCause() {
        Throwable cause = new IllegalStateException("root");
        assertThat(new FoundationTechnicalException("wrapped", cause).getCause())
                .isSameAs(cause);
    }
}
