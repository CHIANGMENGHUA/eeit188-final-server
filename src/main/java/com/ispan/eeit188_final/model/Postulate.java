package com.ispan.eeit188_final.model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "postulate")
public class Postulate {

	@Id
	@Column(name = "id",columnDefinition = "uniqueidentifier")
	private UUID id;
	
	@Column(name = "postulate", columnDefinition = "varchar(15)")
	private String postulate;
	
	@Column(name = "createdAt", columnDefinition = "datetime2")
	private Timestamp createdAt;
	
	
}