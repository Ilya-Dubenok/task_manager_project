package org.example.core.dto;

import java.util.List;

public class PageOfUserDTO {

    private Integer number;

    private Integer size;

    private Integer totalPages;

    private Long totalElements;

    private boolean first;

    private Integer numberOfElements;

    private boolean last;

    private List<UserDTO> content;


    public PageOfUserDTO() {
    }

    public PageOfUserDTO(Integer number, Integer size, Integer totalPages, Long totalElements,
                         boolean first, Integer numberOfElements, boolean last, List<UserDTO> content) {
        this.number = number;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.first = first;
        this.numberOfElements = numberOfElements;
        this.last = last;
        this.content = content;
    }

    /**
     * Returns the number of the current requested page
     * @return number of the current requested page
     */
    public Integer getNumber() {
        return number;
    }

    /**
     *Sets the number of the current requested page
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * Returns the number of requested elements per page
     * @return number of requested elements per page
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Set the number of requested elements per page
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Returns the total number of pages that contain elements which satisfy the request
     * @return  total number of pages that contain elements which satisfy the request
     */
    public Integer getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total number of pages that contain elements which satisfy the request
     */
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Returns total number of elements that satisfy the request
     * @return total number of elements that satisfy the request
     */
    public Long getTotalElements() {
        return totalElements;
    }

    /**
     * Sets total number of elements that satisfy the request
     */
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Returns if the current returned page is first page of rows of pages
     * @return if the current returned page is first page of rows of pages
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Sets if the current returned page is first page of rows of pages
     */
    public void setFirst(boolean first) {
        this.first = first;
    }

    /**
     * Returns the number of elements on the <b>current</b> page
     * @return the number of elements on the <b>current</b> page
     */
    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    /**
     * Sets the number of elements on the <b>current</b> page
     */
    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    /**
     * Return if the current page is last page
     * @return if the current page is last page
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Sets if the current page is last page
     */
    public void setLast(boolean last) {
        this.last = last;
    }

    /**
     * Returns List of UserDTOs on the current page
     * @return List of UserDTOs on the current page
     */
    public List<UserDTO> getContent() {
        return content;
    }

    /**
     * Sets List of UserDTOs
     */
    public void setContent(List<UserDTO> content) {
        this.content = content;
    }
}
