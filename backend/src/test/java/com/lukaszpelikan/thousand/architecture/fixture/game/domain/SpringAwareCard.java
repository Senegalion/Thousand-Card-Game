package com.lukaszpelikan.thousand.architecture.fixture.game.domain;

import org.springframework.context.ApplicationContext;

public record SpringAwareCard(ApplicationContext context) {}
