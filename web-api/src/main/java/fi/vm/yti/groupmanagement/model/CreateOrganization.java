package fi.vm.yti.groupmanagement.model;

import java.util.List;
import java.util.UUID;

public class CreateOrganization {

    public String url;
    public String nameFi;
    public String nameEn;
    public String nameSv;
    public String descriptionFi;
    public String descriptionEn;
    public String descriptionSv;
    public UUID parentId;

    public List<String> adminUserEmails;
}
