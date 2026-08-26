# Graph Report - track-mobile  (2026-08-26)

## Corpus Check
- 12 files · ~18,562 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 290 nodes · 584 edges · 32 communities (19 shown, 13 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 22 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Dashboard & Activity Feed UI
- Detail Screen & History Composable
- Manage Unit Screen & Operations
- Application Entry & ViewModel Factory
- Monitor Screen & Daily Summary
- Common UI Components & Shared Dropdown
- Manage Components & Add Form
- Detail Components & Condition Editor
- Retrofit API Service & Endpoints
- Monitor Components & Filter Panel
- Scooter Repository & Data Gateway
- Device Condition Diagnostics
- CSV & Domain Exporter Utilities
- QR Code Generation & PNG Utils
- Scooter Data ViewModel & State
- API Client & Feature Concepts
- Gradle Wrapper Scripts
- Manage ViewModel & State
- Date & Time Formatting Utilities
- CI/CD & App Build Configuration
- Community 20
- Community 21
- Community 22
- Community 23
- Community 24
- Community 25
- Community 26
- Community 28
- Community 29

## God Nodes (most connected - your core abstractions)
1. `Scooter` - 25 edges
2. `ScooterRepository` - 20 edges
3. `ActivityLogEntry` - 19 edges
4. `DateUtils` - 16 edges
5. `DashboardScreen()` - 15 edges
6. `ManageScreen()` - 14 edges
7. `ApiService` - 13 edges
8. `MaintenanceRecord` - 13 edges
9. `ScooterDataViewModel` - 12 edges
10. `ManageViewModel` - 12 edges

## Surprising Connections (you probably didn't know these)
- `ScooterDetailScreen()` --calls--> `ConditionEditor()`  [INFERRED]
  app/src/main/java/com/evrenhouse/trackscooter/ui/detail/ScooterDetailScreen.kt → app/src/main/java/com/evrenhouse/trackscooter/ui/detail/DetailComponents.kt
- `ScooterDetailScreen()` --calls--> `HistorySection()`  [INFERRED]
  app/src/main/java/com/evrenhouse/trackscooter/ui/detail/ScooterDetailScreen.kt → app/src/main/java/com/evrenhouse/trackscooter/ui/detail/DetailComponents.kt
- `ManageScreen()` --calls--> `ManageActionButton()`  [INFERRED]
  app/src/main/java/com/evrenhouse/trackscooter/ui/manage/ManageScreen.kt → app/src/main/java/com/evrenhouse/trackscooter/ui/manage/ManageComponents.kt
- `MonitorScreen()` --calls--> `HistoricalSummary()`  [INFERRED]
  app/src/main/java/com/evrenhouse/trackscooter/ui/monitor/MonitorScreen.kt → app/src/main/java/com/evrenhouse/trackscooter/ui/monitor/MonitorComponents.kt
- `DashboardScreen()` --calls--> `HistoryFilters`  [INFERRED]
  app/src/main/java/com/evrenhouse/trackscooter/ui/dashboard/DashboardScreen.kt → app/src/main/java/com/evrenhouse/trackscooter/ui/dashboard/DashboardComponents.kt

## Import Cycles
- None detected.

## Communities (32 total, 13 thin omitted)

### Community 0 - "Dashboard & Activity Feed UI"
Cohesion: 0.09
Nodes (13): ApiService, ResponseBody, ActiveMaintenance, ApiError, CompleteMaintenanceResponse, DeviceFields, SaveDeviceConditionResponse, ToggleRequest (+5 more)

### Community 1 - "Detail Screen & History Composable"
Cohesion: 0.11
Nodes (16): DashboardData, ScooterRepository, MainActivity, TrackScooterApp, AppViewModelFactory, repository(), AppNavHost(), BottomNavItem (+8 more)

### Community 2 - "Manage Unit Screen & Operations"
Cohesion: 0.14
Nodes (22): android, ScooterStatus, EmptyState(), ErrorState(), FilledAction(), Color, Modifier, OutlinedAction() (+14 more)

### Community 3 - "Application Entry & ViewModel Factory"
Cohesion: 0.18
Nodes (24): ActivityLogEntry, MaintenanceRecord, LiveTimer(), TypeBadge(), ActivityFeedCard(), FeedRow(), FilterDropdown(), HistoryFilters (+16 more)

### Community 4 - "Monitor Screen & Daily Summary"
Cohesion: 0.17
Nodes (16): AddScooterRequest, Scooter, UpdateScooterRequest, SimpleDropdown(), AddScooterForm(), compareScooters(), ManageActionButton(), numericPart() (+8 more)

### Community 5 - "Common UI Components & Shared Dropdown"
Cohesion: 0.20
Nodes (14): androidx, SaveDeviceConditionRequest, LoadingState(), StatusChip(), ScooterDetailScreen(), ActivityFeedPanel(), MonitorFilterPanel(), MonitorFilterTab() (+6 more)

### Community 7 - "Detail Components & Condition Editor"
Cohesion: 0.20
Nodes (8): DeviceCondition, DeviceConditionHelper, FieldTone, BAD, GOOD, NONE, WARN, Issue

### Community 8 - "Retrofit API Service & Endpoints"
Cohesion: 0.19
Nodes (5): ByteArray, StateFlow, ViewModel, ManageUiState, ManageViewModel

### Community 9 - "Monitor Components & Filter Panel"
Cohesion: 0.23
Nodes (7): ByteArray, QrUtils, toPngBytes(), ByteArray, Context, QrZip, Bitmap

### Community 10 - "Scooter Repository & Data Gateway"
Cohesion: 0.20
Nodes (8): ScooterType, ActionLabels, DeviceField, DeviceFields, DeviceLabels, StatusLabels, StatusOrder, TypeLabels

### Community 11 - "Device Condition Diagnostics"
Cohesion: 0.22
Nodes (9): ApiClient, OkHttpClient, API Configuration & Endpoints, Dashboard Feature, Detail Unit Feature, Kelola Unit Feature, Monitor Feature, Scan QR Feature (+1 more)

### Community 12 - "CSV & Domain Exporter Utilities"
Cohesion: 0.33
Nodes (5): StateFlow, ViewModel, ScooterDataUiState, ScooterDataViewModel, Job

### Community 13 - "QR Code Generation & PNG Utils"
Cohesion: 0.36
Nodes (4): DetailUiState, StateFlow, ViewModel, ScooterDetailViewModel

### Community 14 - "Scooter Data ViewModel & State"
Cohesion: 0.43
Nodes (7): ConditionEditor(), FieldEditor(), HistoryRow(), HistorySection(), Color, Modifier, DeviceField

### Community 15 - "API Client & Feature Concepts"
Cohesion: 0.50
Nodes (4): errorBodyMessage(), ByteArray, readBytesOrNull(), toUserMessage()

### Community 16 - "Gradle Wrapper Scripts"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **17 isolated node(s):** `DeviceLabels`, `StatusOrder`, `BottomNavItem`, `BAD`, `GOOD` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Scooter` connect `Monitor Screen & Daily Summary` to `Dashboard & Activity Feed UI`, `Detail Screen & History Composable`, `Application Entry & ViewModel Factory`, `Common UI Components & Shared Dropdown`, `Retrofit API Service & Endpoints`, `CSV & Domain Exporter Utilities`, `QR Code Generation & PNG Utils`?**
  _High betweenness centrality (0.180) - this node is a cross-community bridge._
- **Why does `ApiService` connect `Dashboard & Activity Feed UI` to `Device Condition Diagnostics`, `Monitor Screen & Daily Summary`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Why does `ScooterRepository` connect `Detail Screen & History Composable` to `Dashboard & Activity Feed UI`, `Monitor Screen & Daily Summary`, `Detail Components & Condition Editor`, `Retrofit API Service & Endpoints`, `CSV & Domain Exporter Utilities`, `QR Code Generation & PNG Utils`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `DashboardScreen()` (e.g. with `ActivityFeedCard()` and `FilterDropdown()`) actually correct?**
  _`DashboardScreen()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DeviceLabels`, `StatusOrder`, `BottomNavItem` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Dashboard & Activity Feed UI` be split into smaller, more focused modules?**
  _Cohesion score 0.08817204301075268 - nodes in this community are weakly interconnected._
- **Should `Detail Screen & History Composable` be split into smaller, more focused modules?**
  _Cohesion score 0.10574712643678161 - nodes in this community are weakly interconnected._