package com.example.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PortfolioApplicationTests {

    @Test
    void applicationEntrypointExists() {
        assertThat(PortfolioApplication.class).isNotNull();
    }
}
