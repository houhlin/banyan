package com.freedom.messagebus.server.dataaccess;

import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.business.model.ReceivePermission;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReceivePermissionFetcher implements IDataFetcher {

    private static final Log logger = LogFactory.getLog(ReceivePermissionFetcher.class);

    private DBAccessor dbAccessor;

    public ReceivePermissionFetcher(DBAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public byte[] fetchData(IDataConverter converter) {
        ArrayList<ReceivePermission> receivePermissions = new ArrayList<>();

        String sql = "SELECT * FROM RECEIVE_PERMISSION ";

        try (Connection connection = this.dbAccessor.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ReceivePermission receivePermission = new ReceivePermission();
                receivePermission.setTargetId(rs.getInt("targetId"));
                receivePermission.setGrantId(rs.getInt("grantId"));

                receivePermissions.add(receivePermission);
            }
        } catch (SQLException e) {
            ExceptionHelper.logException(logger, e, "fetchData");
            throw new RuntimeException(e);
        }

        ReceivePermission[] receivePermissionArr = receivePermissions.toArray(new ReceivePermission[receivePermissions.size()]);

        return converter.serialize(receivePermissionArr);
    }
}
