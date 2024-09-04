package com.ispan.eeit188_final.model;

import java.util.Objects;
import java.util.UUID;

//@Entity
//@Table(name = "cart")
public class CartId {

	private UUID userId;
	private UUID houseId;

	@Override
	public int hashCode() {
		return Objects.hash(userId,houseId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj==null || getClass() != obj.getClass()) {
			return false;
		}
		CartId cartId = (CartId) obj;
		return Objects.equals(userId, cartId.userId)
				&&Objects.equals(houseId, cartId.houseId);
	}
	
}
