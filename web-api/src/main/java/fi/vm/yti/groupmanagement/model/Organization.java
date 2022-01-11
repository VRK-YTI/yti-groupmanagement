package fi.vm.yti.groupmanagement.model;

import java.util.UUID;

public class Organization {

    public UUID id;
    public String url;
    public String nameFi;
    public String nameEn;
    public String nameSv;
    public String descriptionFi;
    public String descriptionEn;
    public String descriptionSv;
    public boolean removed;
    public UUID parentId;
}
