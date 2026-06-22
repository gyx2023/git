package com.crm.system.service;

import com.crm.system.domain.dao.PersonnelInfo;

import java.util.List;

public interface PersonnelInfoService {

    List<PersonnelInfo> findList(PersonnelInfo personnelInfo);

    void insertOrUpdate(PersonnelInfo personnelInfo);

    void insert(PersonnelInfo personnelInfo);
}
