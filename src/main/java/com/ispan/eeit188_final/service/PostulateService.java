package com.ispan.eeit188_final.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.ispan.eeit188_final.dto.PostulateDTO;
import com.ispan.eeit188_final.model.House;
import com.ispan.eeit188_final.model.Postulate;
import com.ispan.eeit188_final.repository.HouseRepository;
import com.ispan.eeit188_final.repository.PostulateRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PostulateService {

	@Autowired
	private PostulateRepository postulateRepository;

	@Autowired
    private HouseRepository houseRepository;

    public Page<House> getHouses(PostulateDTO postulateDTO) {
		// 預設 頁數 限制
        Integer defaultPage = 0;
        Integer defaultLimit = 10;
        // 頁數 限制 排序
        Integer page = Optional.ofNullable(postulateDTO.getPage()).orElse(defaultPage);
        Integer limit = Optional.ofNullable(postulateDTO.getLimit()).orElse(defaultLimit);
        Boolean dir = Optional.ofNullable(postulateDTO.getDir()).orElse(false);
        String order = Optional.ofNullable(postulateDTO.getOrder()).orElse(null);
		// 是否排序
        Sort sort = (order != null) ? Sort.by(dir ? Direction.DESC : Direction.ASC, order) : Sort.unsorted();
        return houseRepository.findByPostulateId(postulateDTO.getPostulateId(), PageRequest.of(page, limit, sort));
    }

	public Postulate findById(UUID id) {
		if (id != null) {
			Optional<Postulate> optional = postulateRepository.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}

	public List<Postulate> findAll() {
		return postulateRepository.findAll();
	}

	public Page<Postulate> findAll(String json) {
		Integer defalutPageNum = 0;
		Integer defaultPageSize = 10;
		
		JSONObject obj = new JSONObject(json);
		
		Integer pageNum = obj.isNull("pageNum") ? defalutPageNum : obj.getInt("pageNum");
		Integer pageSize = obj.isNull("pageSize") || obj.getInt("pageSize") == 0 ? defaultPageSize : obj.getInt("pageSize");
		Boolean desc = obj.isNull("desc") ? false : obj.getBoolean("desc");
		String orderBy = obj.isNull("orderBy") || obj.getString("orderBy").length() == 0 ? "id" : obj.getString("orderBy");

		Pageable p = PageRequest.of(pageNum, pageSize, desc ? Direction.ASC : Direction.DESC, orderBy);

		return postulateRepository.findAll(p);
	}
	
	public Postulate findByName(String name) {
		if (name != null && name.length() != 0) {
			List<Postulate> postulates = postulateRepository.findByPostulate(name);
			if (postulates != null && postulates.size() != 0) {
				return postulates.get(0);
			}
		}
		return null;
	}
	
	public void deleteById(UUID id) {
		if (id != null) {
			postulateRepository.deleteById(id);
		}
	}

	public void deleteAll() {
		postulateRepository.deleteAll();
	}

	public Postulate create(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			String postulate = obj.isNull("postulate") ? null : obj.getString("postulate");

			Postulate insert = new Postulate();
			insert.setPostulate(postulate);

			return postulateRepository.save(insert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Postulate create(Postulate postulate) {
		if (postulate.getCreatedAt() == null) {
			postulate.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		}
		return postulateRepository.save(postulate);
	}
	
	public Postulate update(String json) {
		try {
			JSONObject obj = new JSONObject(json);
			UUID id = obj.isNull("id") ? null : UUID.fromString(obj.getString(json));
			Postulate dbData = findById(id);
			if (dbData != null) {
				dbData.setPostulate(obj.isNull("postulate") ? null : obj.getString("postulate"));
				return dbData;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Postulate update(Postulate postulate) {
		Optional<Postulate> dbPostulate = postulateRepository.findById(postulate.getId());
		if (dbPostulate.isPresent()) {
			return postulateRepository.save(postulate);
		}
		return null;
	}
	
	
}
