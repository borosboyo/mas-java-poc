package com.optahaul.mas_java_poc.service;

class TenantNotFoundException extends RuntimeException {
	public TenantNotFoundException(String message) {
		super(message);
	}
}
