# Projects Feature — Implementation Plan

Cross-stack port of the "Projects" system (district + science + culture/concert) from the
old codebase (`C:\CodeGarbage\monopoly-backend` / `monopoly-frontend`) to the new
backend (`C:\monopoly\civ-monopoly-backend`, pkg `me.civka.monopoly`) and frontend
(`C:\monopoly\civ-monopoly-frontend`).

## Locked decisions

- **Transport:** REST POST endpoints + WS broadcast (mirror the Property buy/upgrade pattern).
  The old backend used STOMP `@MessageMapping`; the new one uses REST. Frontend calls via
  `src/http/requests/*`, not STOMP publish.
- **Event-type mapping** (new `EventType` already defines these, currently unused):
  - `PROJECTS_EDGE` = district-enhancement projects (old `PROJECTS`)
  - `PROJECTS_SCIENCE` = space/science projects (old `SCIENCE_PROJECTS`)
  - `PROJECTS_CULTURE` = concert (old `GIVE_CONCERT`)
- **Victory:** Build a MINIMAL victory system covering all 4 conditions
  (science via `expeditionTurns`, plus military / culture / score). New backend currently
  has NO victory system (no `winner`/`victoryType` on Room, no evaluation, `score` unused).
- **Concert:** Implement as `PROJECTS_CULTURE` (spend gold → random tourism).
- **Wonder-tied effects: DEFER.** Implement the portable `COMMERCIAL_HUB_INVESTMENT_*`
  gold-per-turn effect + non-wonder district projects now. Stub/skip Great Library, Big Ben,
  `WONDER_DISCOUNT_*`, `GOODY_HUT_WONDER_DISCOUNT` with TODOs (no wonder-effect machinery exists).
- **Fidelity:** Replicate old rules faithfully; flag oddities inline.

## Data model (target, new backend)

### Member (add fields) — `repository/entity/Member.java`
- `finishedScienceProjects: List<ScienceProject>` — `@ElementCollection(fetch=EAGER)` +
  `@Enumerated(STRING)` (pattern from `Property.upgrades`).
- `turnsToNextScienceProject: Integer` — `-1` = inactive, `0` = ready, `>0` = countdown.
- `expeditionTurns: Integer` — `-1` = inactive, `0` = complete, `>0` = countdown.
- `additionalEffects: List<AdditionalEffect>` — `@OneToMany(mappedBy="member", EAGER,
  cascade=ALL, orphanRemoval=true)`.

### New enums (`common/`)
- `ScienceProject`: CAMPUS, SATELLITE, MOON, MARS, EXOPLANET, LASER (ordered progression).
- `ProjectType`: BREAD_AND_CIRCUSES, CAMPUS_RESEARCH_GRANTS, COMMERCIAL_HUB_INVESTMENT,
  ENCAMPMENT_TRAINING, HARBOR_SHIPPING, INDUSTRIAL_ZONE_LOGISTICS, THEATER_SQUARE_PERFORMANCES,
  LAUNCH_EARTH_SATELLITE, LAUNCH_MOON_LANDING, LAUNCH_MARS_COLONY, EXOPLANET_EXPEDITION,
  TERRESTRIAL_LASER_STATION.
- `AdditionalEffectType`: COMMERCIAL_HUB_INVESTMENT_1..4 (impl now);
  GOODY_HUT_WONDER_DISCOUNT, WONDER_DISCOUNT_1..4, ALLIANCE (stub/deferred).
- `VictoryType`: MILITARY, CULTURE, SCIENCE, SCORE.

### New entity — `repository/entity/AdditionalEffect.java`
- `reference: UUID` (`@GeneratedValue UUID`, matching new-backend id convention)
- `member: Member` (`@ManyToOne`)
- `type: AdditionalEffectType` (`@Enumerated(STRING)`)
- `turnsLeft: Integer` (`-1` = permanent, `>0` = countdown, `0` = remove)

### Room (add) — `repository/entity/Room.java`
- `winner: String` (username, nullable)
- `victoryType: VictoryType` (`@Enumerated(STRING)`, nullable)

### DTOs
- `MemberDto`: add `finishedScienceProjects`, `turnsToNextScienceProject`, `expeditionTurns`.
- `AdditionalEffectDto`: `type`, `turnsLeft`, `goldPerTurn` (goldPerTurn computed from type+config).
- `RoomDto`: add `winner`, `victoryType`.
- `ProjectMessage` (record): payload + `List<MemberDto>` + `MessageType`
  (PROJECT_CHOICE / SCIENCE_PROJECT / CONCERT or a single PROJECTS type — finalize at impl).

## Config additions — `config/game/GameConfiguration.java` + `game.json`
(old property names → new config keys)
- `science.basicTurnAmount` (→ sets `turnsToNextScienceProject`)
- `science.expeditionTurnAmount` (→ sets `expeditionTurns` on EXOPLANET)
- `science.laserBoost` (→ reduces `expeditionTurns` on LASER)
- `science.cost`
- `concert.cost`, `concert.tourismLowerBound`, `concert.tourismUpperBound`
- `projects`: per-ProjectType effect stats by district level (goldPerTurn/strength/tourism/gold/etc.)
- (already present: `tourismAdditionalThreshold` = 800, used by Culture victory + frontend Wins)

## Backend phases

### Phase 1 — Data model
Create enums (ScienceProject, ProjectType, AdditionalEffectType, VictoryType), `AdditionalEffect`
entity + repository, add Member fields, add Room winner/victoryType, extend DTOs + MapStruct
mappers, extend `GameConfiguration` + `game.json`. Default new Member fields on creation
(finishedScienceProjects=[], turnsToNextScienceProject=-1, expeditionTurns=-1, additionalEffects=[]).

### Phase 2 — Project logic (service + controller)
- `ProjectService`/`Impl` + `ProjectController` (`/api/v1/projects/...`):
  - choose district project (validate ≥N owned in district, apply effect/additionalEffect, delete event)
  - do science project (gating: CAMPUS prereq + spaceport/rocket L4 + sequence; cost; update
    finishedScienceProjects; set turnsToNextScienceProject; EXOPLANET→expeditionTurns;
    LASER→reduce expeditionTurns)
  - do concert (cost; random tourism in bounds; delete event)
- Event generation: extend `EventServiceImpl.handleNewPosition` to emit PROJECTS_EDGE/SCIENCE/CULTURE
  on landing (port trigger rules from old `handleNewPosition`).
- Turn tick: in `GameServiceImpl` (delegateNextTurn/checkForMortgage) decrement
  `turnsToNextScienceProject`, `expeditionTurns` (→ science victory when crosses 0), and
  `additionalEffects.turnsLeft` (remove at 0).
- Victory: `manageVictory(room, member, VictoryType)` sets Room.winner/victoryType/turn=null;
  evaluate military (≥30 props), culture (tourism ≥ maxOther + tourismAdditionalThreshold),
  science (expeditionTurns), score (last player standing).

### Phase 3 — WS / DTOs / broadcasts
- `ProjectMessage` + broadcasts to `/topic/games/{roomReference}`.
- Member gold-per-turn: ensure effect goldPerTurn applied on turn income (port from old).
  Broadcast member updates + winner/victoryType on GameMessage where needed.
- `GET /api/v1/members/{username}/additionalEffects` (or include in member payload) — finalize.

## Frontend phase (Phase 4)
- Consume new member fields + winner/victoryType in `Game.jsx` WS handlers.
- Project/science/concert UI as events (Events.jsx): port Projects, ScienceProjects, GiveConcert.
- `additionalEffects` in a Cashflow view + include in gold-per-turn (TopPanel `totalGpt`).
- Wins tab Science case: wire `finishedScienceProjects` (X/4) + `expeditionTurns` (X/50).
- HTTP requests in `src/http/requests/` for the new REST endpoints.
- Game winner dialog on victory.

## Open items to extract from OLD backend during impl
- Exact `handleNewPosition` rules that trigger PROJECTS/SCIENCE_PROJECTS/GIVE_CONCERT events.
- `makeProjectChoice` per-ProjectType effect values + district-level scaling + requirement counts.
- Science gating specifics (positions for spaceport/rocket, level checks) and costs.
- AdditionalEffectType → goldPerTurn mapping values (COMMERCIAL_HUB_INVESTMENT_1..4).
- Concert tourism bounds + cost.

## Flagged oddities (replicate but noted)
- Science victory triggers when `expeditionTurns` decrements BELOW 0 (off-by-one flavor).
- District-project requirement in old = own ≥3 in district (confirm exact count at impl).
- New `EventType` already splits projects into EDGE/SCIENCE/CULTURE (old had flat PROJECTS +
  separate GIVE_CONCERT) — design follows the new split.
