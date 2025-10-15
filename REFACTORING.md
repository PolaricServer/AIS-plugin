# AIS Channel Refactoring

## Overview
This document describes the refactoring of the AIS plugin to support both TCP and Serial port connections through a class hierarchy.

## Changes Made

### 1. AisChannel (src/AisChannel.java)
- **Changed from concrete class to abstract class**
- Made the class abstract to serve as a base for channel implementations
- Extracted common functionality shared by all channel types:
  - Vessel tracking (`_vessels`, `_messages`)
  - AIS message processing (position, static info, etc.)
  - Packet handling logic (`handlePacket()` method)
  - Common helper methods (`updatePos()`, `updateStatic()`, `getStn()`)
  
- **Abstract methods** that subclasses must implement:
  - `getConfig()` - Load configuration parameters
  - `activate()` - Start the channel service
  - `deActivate()` - Stop the channel service
  - `getJsConfig()` - Get configuration for REST API
  - `setJsConfig()` - Set configuration from REST API

- **Changed visibility** from `private` to `protected` for:
  - Fields: `_conf`, `_chno`, `_vessels`, `_messages`, `log`, `prev_log_time`, `LOG_INTERVAL_MS`
  - These are now accessible to subclasses

### 2. TcpAisChannel (src/TcpAisChannel.java) - NEW FILE
- **Specialization for TCP connections**
- Inherits from `AisChannel`
- Contains TCP-specific fields:
  - `_host` - TCP host address
  - `_port` - TCP port number
  - `reader` - AisTcpReader instance
  
- Implements all abstract methods with TCP-specific logic
- Uses `AisReaders.createReader(_host, _port)` to create TCP reader
- JSON type name: "AIS-TCP"
- Configuration properties:
  - `channel.<id>.host` (default: "localhost")
  - `channel.<id>.port` (default: 4030)

### 3. SerialAisChannel (src/SerialAisChannel.java) - NEW FILE
- **Specialization for Serial port connections**
- Inherits from `AisChannel`
- Contains Serial-specific fields:
  - `_port` - Serial port device path (e.g., "/dev/ttyUSB0")
  - `reader` - AisReader instance
  
- Implements all abstract methods with Serial-specific logic
- Uses `AisReaders.createReaderFromInputStream(new FileInputStream(_port))` to create serial reader
- JSON type name: "AIS-SERIAL"
- Configuration properties:
  - `channel.<id>.port` (default: "/dev/ttyUSB0")

### 4. AisPlugin (src/AisPlugin.java)
- **Updated channel registration**
- Changed from registering single "AIS-TCP" class to registering both:
  - "AIS-TCP" → `no.polaric.ais.TcpAisChannel`
  - "AIS-SERIAL" → `no.polaric.ais.SerialAisChannel`
- Updated `AisChannel.classInit()` to register both JSON subtypes

## Design Benefits

1. **Separation of Concerns**: TCP and Serial logic are cleanly separated into their own classes
2. **Code Reuse**: Common AIS processing logic remains in the base class
3. **Extensibility**: Easy to add new channel types (e.g., UDP, WebSocket) by extending AisChannel
4. **Type Safety**: JSON deserialization properly routes to correct subclass
5. **Backward Compatibility**: Existing TCP configurations continue to work with "AIS-TCP" type

## Usage

### TCP Channel Configuration
```properties
channel.myais.host=ais.example.com
channel.myais.port=4030
```

### Serial Channel Configuration
```properties
channel.myserial.port=/dev/ttyUSB0
```

## JSON Configuration

### TCP Channel
```json
{
  "type": "AIS-TCP",
  "host": "localhost",
  "port": 4030,
  "messages": 1234,
  "vessels": 56
}
```

### Serial Channel
```json
{
  "type": "AIS-SERIAL",
  "port": "/dev/ttyUSB0",
  "messages": 789,
  "vessels": 23
}
```

## Minimal Changes Approach

This refactoring was done with minimal modifications:
- No changes to existing functionality or behavior
- No changes to AisVessel or other classes
- Only moved code between files (from AisChannel to TcpAisChannel)
- Added new SerialAisChannel with similar structure
- Updated plugin registration to recognize both types

## Testing Considerations

- Verify TCP channels continue to work as before
- Test new Serial channels with actual serial devices
- Verify JSON serialization/deserialization for both types
- Test activation/deactivation for both channel types
- Verify proper error handling and resource cleanup
