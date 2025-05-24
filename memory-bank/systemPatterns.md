# System Patterns: Closspad

## Architecture
- Event-driven frontend architecture
- Centralized state management
- Component-based UI

## Key Technical Decisions
- Replicant for DOM rendering (React-like)
- Reitit for routing
- Core.async for async operations
- Custom rating algorithm instead of standard ELO

## Component Relationships
- Core initializes:
  * State management
  * Router
  * Event system
- Views render based on state
- Network layer handles Supabase communication
- Rating system processes match results
