package fi.vm.yti.groupmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.groupmanagement.dao.SystemDao;
import fi.vm.yti.groupmanagement.model.SystemCountModel;

@Service
public class SystemApiService {

    private static final Logger logger = LoggerFactory.getLogger(SystemApiService.class);

    private SystemDao systemDao;

    @Autowired
    public SystemApiService(SystemDao systemDao) {
        this.systemDao = systemDao;
    }

    @Transactional
    public SystemCountModel countThings() {
        SystemCountModel ret = null;
        try {
            ret = systemDao.countThings();
            return ret;
        } finally {
            if (ret != null) {
                logger.info("Inventory: " + ret);
            } else {
                logger.error("Inventory failed");
            }
        }
    }
}
