# AIS Channel Class Hierarchy

```
Channel (from aprsd core)
   |
   +-- AisChannel (abstract)
          |
          +-- Shared functionality:
          |     - Vessel tracking (_vessels, _messages)
          |     - Message parsing and processing (handlePacket)
          |     - Position updates (updatePos, updatePosExtra)
          |     - Static info updates (updateStatic)
          |     - Helper methods (getStn)
          |
          +-- Abstract methods:
          |     - getConfig()
          |     - activate()
          |     - deActivate()
          |     - getJsConfig()
          |     - setJsConfig()
          |
          +-- TcpAisChannel
          |     - Uses AisTcpReader
          |     - Config: host + port
          |     - Type: "AIS-TCP"
          |     - Reader: AisReaders.createReader(host, port)
          |
          +-- SerialAisChannel
                - Uses AisReader
                - Config: port (device path)
                - Type: "AIS-SERIAL"
                - Reader: AisReaders.createReaderFromInputStream(FileInputStream(port))
```

## Method Implementation Matrix

| Method | AisChannel | TcpAisChannel | SerialAisChannel |
|--------|-----------|--------------|------------------|
| `getConfig()` | abstract | Implemented (host+port) | Implemented (device port) |
| `activate()` | abstract | Implemented (TCP) | Implemented (Serial) |
| `deActivate()` | abstract | Implemented | Implemented |
| `getJsConfig()` | abstract | Implemented | Implemented |
| `setJsConfig()` | abstract | Implemented | Implemented |
| `handlePacket()` | Implemented | Inherited | Inherited |
| `updatePos()` | Implemented | Inherited | Inherited |
| `updatePosExtra()` | Implemented | Inherited | Inherited |
| `updateStatic()` | Implemented | Inherited | Inherited |
| `getStn()` | Implemented | Inherited | Inherited |

## JSON Configuration Types

### AIS-TCP Configuration
```json
{
  "type": "AIS-TCP",
  "host": "ais.example.com",
  "port": 4030,
  "messages": 1234,
  "vessels": 56
}
```

### AIS-SERIAL Configuration
```json
{
  "type": "AIS-SERIAL",
  "port": "/dev/ttyUSB0",
  "messages": 789,
  "vessels": 23
}
```

## Plugin Registration

```java
// In AisPlugin.activate():
_conf.getChanManager().addClass("AIS-TCP", "no.polaric.ais.TcpAisChannel");
_conf.getChanManager().addClass("AIS-SERIAL", "no.polaric.ais.SerialAisChannel");
AisChannel.classInit();
```

## Design Pattern

This refactoring implements the **Template Method Pattern**:
- The abstract base class (AisChannel) defines the skeleton of the algorithm
- Subclasses override specific steps without changing the overall structure
- Common behavior is centralized in the base class
- Specialization happens only where needed

## Benefits

1. **Code Reuse**: All AIS message processing logic is shared
2. **Extensibility**: Easy to add new channel types (UDP, WebSocket, etc.)
3. **Type Safety**: Compile-time checking of method implementations
4. **Maintainability**: Changes to common logic happen in one place
5. **Backward Compatibility**: Existing TCP configurations still work
