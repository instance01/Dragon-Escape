package com.comze_instancelabs.dragonescape;

import java.util.ArrayList;

import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.EntityComplexPart;
import net.minecraft.server.v1_7_R1.EntityEnderDragon;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.MathHelper;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Test extends EntityEnderDragon {

	private boolean onGround = false;
	private ArrayList<Vector> points = new ArrayList();
	private int currentid;
	private double X;
	private double Y;
	private double Z;

	public Test(Location loc, World world, ArrayList<Vector> p) {
		super(world);
		currentid = 0;
		this.points = p;
		setPosition(loc.getX(), loc.getY(), loc.getZ());
		yaw = loc.getYaw() + 180;
		while (yaw > 360) {
			yaw -= 360;
		}
		while (yaw < 0) {
			yaw += 360;
		}
		if (yaw < 45 || yaw > 315) {
			yaw = 0F;
		} else if (yaw < 135) {
			yaw = 90F;
		} else if (yaw < 225) {
			yaw = 180F;
		} else {
			yaw = 270F;
		}
		
		double disX = (this.locX - points.get(currentid).getX());
		double disY = (this.locY - points.get(currentid).getY());
		double disZ = (this.locZ - points.get(currentid).getZ());

		double tick = Math.sqrt(disX * disX + disY * disY + disZ * disZ) * 2;

		this.X = (Math.abs(disX) / tick);
		this.Y = (Math.abs(disY) / tick);
		this.Z = (Math.abs(disZ) / tick);
	}

	@Override
	public void e() {
		return;
	}

	public boolean damageEntity(DamageSource damagesource, int i) {
		return false;
	}

	@Override
	public int getExpReward() {
		return 0;
	}

	public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, int i) {
		return false;
	}

	public Vector getNextPosition() {
		
		double tempx = this.locX;
		double tempy = this.locY;
		double tempz = this.locZ;


		if (tempx < points.get(currentid).getX())
			tempx += this.X;
		else {
			tempx -= this.X;
		}

		if ((int) tempy < points.get(currentid).getY()) {
			tempy += this.Y;
		} else {
			tempy -= this.Y;
		}

		if (tempz < points.get(currentid).getZ())
			tempz += this.Z;
		else {
			tempz -= this.Z;
		}


		if (((Math.abs((int) tempx - points.get(currentid).getX()) == 0) && (Math.abs((int) tempz - points.get(currentid).getZ()) <= 3)) || ((Math.abs((int) tempz - points.get(currentid).getZ()) == 0) && (Math.abs((int) tempx - points.get(currentid).getX()) <= 3) && (Math.abs((int) tempy - points.get(currentid).getY()) <= 5))) {
			if (currentid < points.size() - 1) {
				currentid += 1;
			} else {
				// finish
			}

			double disX = (this.locX - points.get(currentid).getX());
			double disY = (this.locY - points.get(currentid).getY());
			double disZ = (this.locZ - points.get(currentid).getZ());

			double tick_ = Math.sqrt(disX * disX + disY * disY + disZ * disZ) * 2;

			this.X = (Math.abs(disX) / tick_);
			this.Y = (Math.abs(disY) / tick_);
			this.Z = (Math.abs(disZ) / tick_);
		}

		return new Vector(tempx, tempy, tempz);
	}

}