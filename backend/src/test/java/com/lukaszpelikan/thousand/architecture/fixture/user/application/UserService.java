package com.lukaszpelikan.thousand.architecture.fixture.user.application;

import com.lukaszpelikan.thousand.architecture.fixture.identity.api.IdentityFacade;
import com.lukaszpelikan.thousand.architecture.fixture.lobby.api.LobbyFacade;
import com.lukaszpelikan.thousand.architecture.fixture.match.api.MatchFacade;

public record UserService(IdentityFacade identity, LobbyFacade lobby, MatchFacade matches) {}
