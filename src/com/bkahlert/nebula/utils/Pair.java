package com.bkahlert.nebula.utils;

public class Pair<F, S> {
	private final F first;
	private final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}

	@Override
	public String toString() {
		return "{" + this.getFirst() + ", " + this.getSecond() + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.first == null) ? 0 : this.first.hashCode());
		result = prime * result
				+ ((this.second == null) ? 0 : this.second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (this.first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!this.first.equals(other.first)) {
			return false;
		}
		if (this.second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!this.second.equals(other.second)) {
			return false;
		}
		return true;
	}

}