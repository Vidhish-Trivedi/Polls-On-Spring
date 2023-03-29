package com.mypolls.polls.model;

import java.util.List;

public class PagedResponse <T> {
    private List <T> content;
    private int page;
    private int size;
    private Long totalElements;
    private int totalPages;
    private Boolean last;

    public PagedResponse() {

    }


    public PagedResponse(List<T> content, int page, int size, Long totalElements, int totalPages, Boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }


    public List<T> getContent() {
        return this.content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return this.totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean isLast() {
        return this.last;
    }

    public Boolean getLast() {
        return this.last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }
}
