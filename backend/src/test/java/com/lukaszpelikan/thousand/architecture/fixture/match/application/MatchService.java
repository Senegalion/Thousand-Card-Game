package com.lukaszpelikan.thousand.architecture.fixture.match.application;

import com.lukaszpelikan.thousand.architecture.fixture.game.domain.Card;
import com.lukaszpelikan.thousand.architecture.fixture.lobby.api.LobbyFacade;

public record MatchService(Card card, LobbyFacade lobby) {}
