package com.lukaszpelikan.thousand.architecture.fixture.lobby.application;

import com.lukaszpelikan.thousand.architecture.fixture.identity.api.IdentityFacade;
import com.lukaszpelikan.thousand.architecture.fixture.lobby.infrastructure.TableRepository;
import com.lukaszpelikan.thousand.architecture.fixture.shared.SharedClock;

public record LobbyService(IdentityFacade identity, TableRepository tables, SharedClock clock) {}
