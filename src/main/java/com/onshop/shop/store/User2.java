package com.onshop.shop.store;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User2 {
	@Id
	private Integer id;
	private String name;
	private LocalDate birthDate;


}
