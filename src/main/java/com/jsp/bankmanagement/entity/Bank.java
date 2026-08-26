package com.jsp.bankmanagement.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bank 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String bankName;
	@Column(unique = true)
	private String ifsc;
	private String branchName;
	@Column(unique = true)
	private Long contact;
	
	@JsonIgnore
	@OneToMany(mappedBy = "bank")
	private List<Account> accounts;
	
	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL)
	private Address address;
}
