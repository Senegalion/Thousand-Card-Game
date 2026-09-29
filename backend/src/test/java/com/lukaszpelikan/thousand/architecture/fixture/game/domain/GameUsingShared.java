package com.lukaszpelikan.thousand.architecture.fixture.game.domain;

import com.lukaszpelikan.thousand.architecture.fixture.shared.SharedClock;

public record GameUsingShared(SharedClock clock) {}
