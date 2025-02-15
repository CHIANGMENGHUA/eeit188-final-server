package com.ispan.eeit188_final.service;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import com.ispan.eeit188_final.dto.PostulateDTO;
import com.ispan.eeit188_final.model.Postulate;

@SpringBootTest
public class PostulateServiceTests {

	@Autowired
	private PostulateService postulateService;

	// @Test
	public void testCreate() {
		Postulate postulate = new Postulate();
		postulate.setName("TEST_POSTULATE");
		postulate.setName("AA");

		System.out.println(postulateService.create(postulate));
	}

	// @Test
	public void testFindAll() {
		List<Postulate> list = postulateService.findAll();
		for (Postulate postulate : list) {
			System.out.println(postulate);
		}
	}

	// @Test
	public void testFindById() {
		String id = "05d322e1-42e4-4af1-92c3-33b657b053d2";
		UUID uuid = UUID.fromString(id);
		Postulate postulate = postulateService.findById(uuid);
		System.out.println(postulate);
	}

	// @Test
	public void testFindByName() {
		String name = "AA";
		Postulate postulate = postulateService.findByName(name);
		System.out.println(postulate);
	}

	// @Test
	public void testFindAllPage() {
		Integer pageNum = 0;
		Integer pageSize = 30;
		Boolean desc = false;
		String orderBy = "id";

		PostulateDTO postulateDTO = PostulateDTO.builder()
				.page(pageNum)
				.limit(pageSize)
				.dir(desc)
				.order(orderBy)
				.build();

		// JSONObject obj = new JSONObject()
		// .put("pageNum", pageNum)
		// .put("pageSize", pageSize)
		// .put("desc", desc)
		// .put("orderBy", orderBy);

		// String json = obj.toString();
		Page<Postulate> page = postulateService.findAll(postulateDTO);

		System.out.printf("pageNum=%d, pageSize=%d, desc=%b, orderBy=%s\r\n", pageNum, pageSize, desc,
				orderBy);
		for (Postulate postulate : page) {
			System.out.println(postulate.getId() + " " + postulate.getName());
		}
	}
}
