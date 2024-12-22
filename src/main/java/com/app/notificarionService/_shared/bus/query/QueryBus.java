package com.app.notificarionService._shared.bus.query;

public interface QueryBus {
  <R, Q extends Query<R>> R send(Q query);
}
