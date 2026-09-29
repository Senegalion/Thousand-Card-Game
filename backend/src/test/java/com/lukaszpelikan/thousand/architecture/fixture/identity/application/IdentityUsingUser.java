package com.lukaszpelikan.thousand.architecture.fixture.identity.application;

import com.lukaszpelikan.thousand.architecture.fixture.user.api.UserFacade;

public record IdentityUsingUser(UserFacade users) {}
