
/* Agenta parliecibas */

at(P) :- pos(P,X,Y) & pos(car,X,Y).

battery_load(100).

/* Agenta merki */

!check(cells). // Lai parbauditu katru rezgi

/* Plani */

+!check(cells) : not package(car)
   <- .print("Agent is moving forwards");
      next(cell);
      !check(cells).

+at(P) : post(P)
   <- next(cell);
      !check(cells).

+!move_to_the_next_cell(X, Y)
   <- ?pos(car, X1, Y1);
      !move_to_the_next_cell(X, Y).

+!at(L) : at(L).
+!at(L)
   <- ?pos(L,X,Y);
      move_to_the_next_cell(X,Y);
      !at(L).
