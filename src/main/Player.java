package main;

import vectors.Point2D;

public class Player {
Point2D pos;
Point2D vel;

	public Player(Point2D Position, Point2D Velocity) {
		pos = Position;
		vel = Velocity;
	}
	public void update(double dt){
		pos = Point2D.add(pos, vel.scale(dt));
	}

}
