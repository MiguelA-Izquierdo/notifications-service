package com.app.notificarionService._shared.bus.query;

public interface QueryHandler<Q extends Query<R>, R> {
  R handle(Q query);
}
