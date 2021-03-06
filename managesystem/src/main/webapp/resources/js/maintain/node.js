var NODE = {
    parentNode: null
};

$(document).ready(function () {
    NODE.initJTable();

    $('#nodeTableContainer').jtable('load');

    NODE.registerJQEvents();
});

NODE.initJTable = function () {
    $('#nodeTableContainer').jtable({
        title: '节点列表',
        paging: true,
        pageSize: 10,
        addRecordButton: $(''),
        toolbar: {
            items: [
                {
                    icon: '/resources/component/jtable.2.4.0/themes/lightcolor/add.png',
                    text: '注册节点',
                    click: NODE.popNodeLayer
                }
            ]
        },
        actions: {
            listAction: '/maintain/node/list'
        },
        recordAdded: function (event, data) {
            NODE.getParentNodes(null, true);
        },
        recordsDeleted: function (event, data) {
            NODE.getParentNodes(null, true);
        },
        recordUpdated: function (event, data) {
            NODE.getParentNodes(null, true);
        },
        fields: {
            nodeId: {
                title: "编号",
                key: true,
                list: false,
                create: false,
                edit: false
            },
            name: {
                title: '节点名称',
                width: '6%',
                edit: true
            },
            value: {
                title: '内部名称',
                width: '10%',
                edit: true
            },
            parentId: {
                title: '父节点',
                width: '6%',
                options: NODE.getParentNodes
            },
            type: {
                title: '类型',
                width: '5%',
                options: {0: '交换器', 1: '队列'}
            },
            appId: {
                list: false
            },
            routingKey: {
                list: false
            },
            inner: {
                title: '内部节点',
                width: '6%',
                display: function (data) {
                    if (data.record.inner) {
                        return $('<span>是</span>');
                    } else {
                        return $('<span>否</span>');
                    }
                }
            },
            routerType: {
                title: '路由类型',
                width: '6%',
                options: {fanout: 'fanout', topic: 'topic', head: 'head', direct: 'direct'}
            },
            detail: {
                width: '2%',
                display: function (data) {
                    //detail
                    var detailBtn = $('<img src="/resources/image/detail.png" alt="详情" class="clickStyle" />');
                    detailBtn.click(function () {
                        NODE.popDetailLayer(data.record);
                    });
                    return detailBtn;
                }
            },
            update: {
                width: '2%',
                display: function (data) {
                    var updateHtml;
                    updateHtml = '<img alt="更新" class="clickStyle" src="/resources/image/edit.png" />';

                    $(updateHtml).click(function () {

                    });

                    return $(updateHtml);
                }
            },
            delete: {
                width: '2%',
                display: function (data) {
                    //delete
                    var deleteHtml;
                    deleteHtml = '<img alt="删除" class="clickStyle" src="/resources/image/delete.png" />';
                    $(deleteHtml).click(function () {

                    });

                    return $(deleteHtml);
                }
            }
        }
    });
};

NODE.registerJQEvents = function () {
    //select's option event
    $('#select_node_parentId').change(function () {
        var $selectedItem = $(this).children('option:selected');
        var selectedLevel = $selectedItem.attr('level');
        var intLevel = parseInt(selectedLevel);
        $('#input_node_level').val(intLevel + 1);
    });

    $('#saveForm').on('click', function () {
        var isValidated = NODE.validateForm();
        if (isValidated) {
            NODE.submitNodeInfo();
        } else {
            layer.alert('表单验证失败!', 8);
        }
    });
};

NODE.getParentNodes = function (data, isUpdate) {
    isUpdate = isUpdate || false;
    if (NODE.parentNode != null && !isUpdate)
        return NODE.parentNode;
    else {
        $.ajax({
            url: '/maintain/node/parentNodeInfo',
            type: 'POST',
            contentType: 'application/json;charset=utf-8',
            async: false,
            cache: false,
            success: function (data) {
                if (data && data.Options) {
                    data.Options.push({
                        Value: -1,
                        DisplayText: '无'
                    });

                    NODE.parentNode = data.Options;
                } else {
                    NODE.parentNode = [];
                }
            },
            error: function (err) {
                NODE.parentNode = JSON.parse(err);
            }
        });

        return NODE.parentNode;
    }
};

NODE.popNodeLayer = function (isCreate) {
    var create = isCreate || true;
    $.layer({
        type: 1,
        title: false,
        border: [1],
        shadeClose: false,
        area: ['540px', '300px'],
        page: {dom: '#nodeLayer'},
        close: function () {
            //reload data
            $('#nodeTableContainer').jtable('reload');
        },
        success: function () {
            NODE.clearPopNodeLayerState();
            if (create) {           //create

            } else {                //update

            }
        }
    });
};

NODE.validateForm = function () {
    return NODE.validateLevel() && NODE.validateEmpty($('#input_node_name'));
};

NODE.validateLevel = function () {
    var currentLevel = $('#input_node_level').val();
    if (currentLevel) {
        try {
            var int_level = parseInt(currentLevel);
            if (int_level === -1) {
                return false;
            }

            return true;
        } catch (e) {
            return false;
        }
    } else {
        return false;
    }
};

NODE.validateEmpty = function ($obj) {
    if (!$obj) {
        return false;
    }

    if (!$obj.val()) {
        return false;
    }

    return true;
};

NODE.submitNodeInfo = function () {
    $.ajax({
        url: '/maintain/node/create',
        type: 'POST',
        data: {
            'node.name': $('#input_node_name').val(),
            'node.type': $('#input_node_type').val(),
            'node.routerType': $('#select_node_routerType').val(),
            'node.parentId': $('#select_node_parentId').val(),
            'node.level': $('#input_node_level').val()
        },
        async: false,
        cache: false,
        success: function (data) {
            if (data.Result === 'OK') {
                $('#tip').text('操作成功!');
            } else if (data.Result === 'ERROR') {
                $('#tip').text(data.Message);
            }
        },
        error: function (err) {
            if (err) {
                if (err.Result === 'ERROR') {
                    $('#tip').text(data.Message);
                }
            }
        }
    });
};

NODE.clearPopNodeLayerState = function () {
    $('#input_node_name').val('');
    $('#input_node_level').val('');
    $('#input_node_type').get(0).selectedIndex = 0;
    $('#select_node_routerType').get(0).selectedIndex = 0;
    $('#select_node_parentId').get(0).selectedIndex = 0;

    $('#tip').text('');
};

NODE.popDetailLayer = function (record) {
    var jqNodeName = $('#lblNodeName');
    var jqNodeValue = $('#lblNodeValue');
    var jqRouterType = $('#lblRouterType');
    var jqParentId = $('#lblParentNode');
    var jqType = $('#lblType');
    var jqInner = $('#lblInner');
    var jqRoutingKey = $('#lblRoutingKey');
    var jqAvailable = $('#lblAvailable');
    var jqAppId = $('#lblAppId');

    //clear state
    jqNodeName.text('');
    jqNodeValue.text('');
    jqRouterType.text('');
    jqParentId.text('');
    jqType.text('');
    jqInner.text('');
    jqRoutingKey.text('');
    jqAppId.text('');

    //set
    jqNodeName.text(record.name);
    jqNodeValue.text(record.value);
    jqRouterType.text(record.routerType);

    if (NODE.parentNode) {
        var parentNodeName = "";
        for (var i = 0; i < NODE.parentNode.length; i++) {
            if (NODE.parentNode[i].Value == record.parentId) {
                parentNodeName = NODE.parentNode[i].DisplayText;
                break;
            }
        }
        jqParentId.text(parentNodeName);
    }

    jqType.text((record.type == 0 ? "交换器" : "队列"));
    jqInner.text((record.inner == 0 ? "否" : "是"));
    jqRoutingKey.text(record.routingKey);
    jqAvailable.text((record.available == 0 ? "未激活" : "已激活"));
    jqAppId.text(record.appId);


    $.layer({
        type: 1,
        title: false,
        border: [1],
        shadeClose: false,
        area: ['540px', '300px'],
        page: {dom: '#detailLayer'}
    });
};

