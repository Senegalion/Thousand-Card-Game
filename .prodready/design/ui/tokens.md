# Design Tokens

These are implemented as Tailwind CSS 4 `@theme` variables. The UI language is Polish (A-13), with all strings kept in one `pl.ts` module. The layout is mobile-first and responsive, with a minimum width of 360 px.

## Colors

### Brand / table
- felt: #1F6F4A (the card table surface)
- felt-dark: #14523A
- primary: #C8A24A (gold accent for primary actions)
- primary-dark: #A8842F
- on-primary: #1A1A1A

### Cards
- card-face: #FFFFFF
- card-border: #D1D5DB
- suit-red (♥ ♦): #C62828
- suit-black (♠ ♣): #1A1A1A
- card-selected-ring: #C8A24A
- card-illegal-overlay: rgba(0,0,0,0.35). Cards the player can't legally play are dimmed. This is a UX hint only, and the server re-validates.

### Semantic
- success: #22C55E
- warning: #F59E0B (turn timer below 15 s, player disconnected)
- error: #EF4444
- info: #3B82F6
- trump-highlight: #FDE68A

### Neutral
- background: #0F172A (app shell)
- surface: #1E293B (panels, dialogs)
- surface-light: #F9FAFB (forms)
- text-primary: #F8FAFC (on dark)
- text-primary-light: #111827 (on light)
- text-secondary: #94A3B8
- border: #334155

Contrast: text/background pairs meet WCAG AA (4.5:1). Suits are never conveyed by colour alone; the symbol is always shown.

## Typography
- font-family: Inter, system-ui, sans-serif, with full Latin Extended-A support for Polish diacritics
- font-family-cards: "Inter Tight", system-ui (card ranks)
- font-size-xs: 0.75rem
- font-size-sm: 0.875rem
- font-size-base: 1rem
- font-size-lg: 1.125rem
- font-size-xl: 1.25rem
- font-size-2xl: 1.5rem
- font-size-score: 1.75rem (tabular numerals)

## Spacing
- spacing-1: 0.25rem
- spacing-2: 0.5rem
- spacing-3: 0.75rem
- spacing-4: 1rem
- spacing-6: 1.5rem
- spacing-8: 2rem
- card-overlap: -1.75rem on mobile, -1rem on desktop (horizontal overlap of cards in the hand)

## Card sizes
- card-w-sm: 3.5rem, card-h-sm: 5rem (mobile)
- card-w-md: 4.5rem, card-h-md: 6.5rem (desktop)

## Border Radius
- radius-sm: 0.25rem
- radius-md: 0.375rem
- radius-lg: 0.5rem
- radius-card: 0.5rem
- radius-full: 9999px

## Shadows
- shadow-sm: 0 1px 2px rgba(0,0,0,0.05)
- shadow-md: 0 4px 6px rgba(0,0,0,0.1)
- shadow-lg: 0 10px 15px rgba(0,0,0,0.1)
- shadow-card: 0 2px 4px rgba(0,0,0,0.25)

## Motion
- duration-fast: 120ms (card hover and select)
- duration-base: 200ms (card played to the trick)
- Honour `prefers-reduced-motion` by disabling card movement animations.
