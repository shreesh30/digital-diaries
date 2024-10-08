package com.scribblenetwork.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scribblenetwork.entity.ContentEntity;
import com.scribblenetwork.exception.ScribbleException;
import com.scribblenetwork.model.ContentModel;
import com.scribblenetwork.repository.ContentRepository;
import com.scribblenetwork.utils.ContentUtils;
import com.scribblenetwork.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ContentServiceImpl implements ContentService{

    Logger logger= LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ContentRepository contentRepository;

    @Autowired
    ContentServiceImpl(ContentRepository contentRepository){
        this.contentRepository=contentRepository;
    }

    @Transactional(rollbackFor = ScribbleException.class)
    @Override
    public ContentModel createContent(ContentModel contentModel) throws ScribbleException {
        try {
            ContentEntity content=createContentEntity(contentModel);
            content=contentRepository.save(content);
            contentModel=createContentModel(content);

            return contentModel;
        }catch (Exception e){
            throw new ScribbleException("Error saving content "+e);
        }
    }

    @Override
    public ContentModel deleteContent(String contentId) throws ScribbleException {
        try {
            ContentEntity contentEntity=contentRepository.findById(contentId);
            contentRepository.delete(contentEntity);
            ContentModel contentModel=createContentModel(contentEntity);
            return contentModel;
        }catch(Exception e){
            throw new ScribbleException("Error saving content "+e);
        }
    }

    private ContentModel createContentModel(ContentEntity contentEntity) throws JsonProcessingException {
        ContentModel contentModel= new ContentModel();
        contentModel.setId(contentEntity.getId());
        contentModel.setName(contentEntity.getName());
        Map<String,String> data=objectMapper.readValue(contentEntity.getData(), new TypeReference<>() {
        });
        contentModel.setData(data);
        contentModel.setCreatedOn(contentEntity.getCreatedOn());
        contentModel.setLastUpdated(contentEntity.getLastUpdated());

        return contentModel;
    }

    private ContentEntity createContentEntity(ContentModel contentModel) throws JsonProcessingException {
        ContentEntity contentEntity = new ContentEntity();
        String contentId = ContentUtils.generateContentId();
        Long createdOn = System.currentTimeMillis();
        Long lastUpdated = System.currentTimeMillis();

        contentEntity.setId(contentId);
        contentEntity.setName(contentModel.getName());
        contentEntity.setData(objectMapper.writeValueAsString(contentModel.getData()));
        contentEntity.setUserId(UserUtils.getLoggedInUser());
        contentEntity.setCreatedOn(createdOn);
        contentEntity.setLastUpdated(lastUpdated);

        return contentEntity;
    }
}
