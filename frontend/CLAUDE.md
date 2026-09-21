# Frontend Development Context

## Stack

The frontend uses:

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Zustand

## Architecture

Organize the frontend by feature rather than only by technical type.

The intended structure is similar to:

    src/
    ├── app/
    ├── pages/
    ├── features/
    │   ├── auth/
    │   ├── lobby/
    │   ├── game/
    │   └── profile/
    ├── components/
    ├── hooks/
    ├── services/
    │   ├── api/
    │   └── websocket/
    ├── stores/
    └── types/

The exact structure may evolve with the application.

Do not create directories or abstractions without a concrete need.

## TypeScript

Use strict TypeScript.

Avoid `any` unless there is a documented and unavoidable reason.

Prefer explicit domain/application types.

Do not duplicate backend domain rules in the frontend.

The frontend may provide UX validation, but the backend remains authoritative.

## React

Prefer functional components.

Keep components focused on presentation and UI behavior.

Do not place large amounts of business logic inside React components.

Extract reusable application logic into appropriate hooks, services, or state stores.

## State Management

Use Zustand for client application state where global/shared state is required.

Do not put every piece of local component state into Zustand.

Keep server state and transient UI state conceptually separate.

## API

Keep HTTP communication in dedicated API/service modules rather than scattering `fetch` calls throughout components.

Handle loading, error, and authentication states explicitly.

## WebSocket

Keep WebSocket connection and messaging logic separate from UI components.

The frontend consumes authoritative state/events received from the backend.

Never assume that a locally predicted game action was accepted by the server.

## Styling

Use Tailwind CSS.

Prefer reusable components for repeated UI patterns.

Avoid unnecessary custom CSS when Tailwind or existing components are sufficient.

## Testing

Test important user-facing behavior rather than implementation details.

End-to-end tests should cover critical flows such as:

- registration
- login
- creating a room
- joining a room
- starting a game
- playing a game