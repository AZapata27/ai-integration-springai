package com.example.openiaintegration.service;

import java.util.List;

public interface ICRUD<T, ID> {

    T save(T t) throws Exception;
    List<T> saveAll(List<T> t) throws Exception;
    T update(T t, ID id) throws Exception;
    List<T> findAll() throws Exception;
    T findById(ID id) throws Exception;
    void delete(ID id) throws Exception;
}