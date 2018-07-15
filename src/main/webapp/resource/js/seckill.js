    //模块化开发
var seckill = {
    URL:{
        now : function () {
            return '/seckill/time/now';
        },
        exposer : function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },
        killUrl : function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },
    handleSeckill : function(seckillId, node){
      //处理秒杀逻辑
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId),{},function (result) {
            if(result && result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.killUrl(seckillId, md5);
                    console.log(killUrl);
                    //绑定一次点击事件，避免向服务器端发送大量重复请求
                    $('#killBtn').one('click',function () {
                        //执行秒杀
                        //1、先禁用
                        $(this).addClass('disabled');
                        //2、发送请求执行秒杀
                        $.post(killUrl,{},function (result) {
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+ stateInfo +'</span>');
                            }else {
                                console.log(result)
                            }
                        });
                    });
                    node.show();
                }else {
                    //为开启秒杀(客户端时间存在误差的可能，例如客户端已经显示开始秒杀，但是服务端并没有开启)
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //重新计时
                    seckill.countdown(seckillId, now, start, end);
                }
            }else {
                console.log(result);
            }
        });
    },
    validatePhone : function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else {
            return false;
        }
    },
    countdown : function(seckillId, nowTime, startTime, endTime){
        var seckillBox = $('#seckill-box');
        if(nowTime > endTime){
            seckillBox.html('秒杀结束');
        }else if(nowTime < startTime){
            var killTime = new Date(startTime + 1000);
            seckillBox.countdown(killTime,function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
            }).on('finish.countdown',function () {
                //时间完成后回调
                seckill.handleSeckill(seckillId, seckillBox);
            });
        }else{
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    detail:{
        //详情页初始化
        init : function (params) {
           //手机验证和登陆，计时交互
           var killPhone = $.cookie('killPhone');
           //验证手机号（是否登陆）
            //未登录
           if (!seckill.validatePhone(killPhone)){
               //绑定手机号
               var killPhoneModal = $('#killPhoneModal');
               //显示弹出层
               killPhoneModal.modal({
                   show : true,
                   backdrop : 'static',
                   keyboard : false
               });
               $('#killPhoneBtn').click(function () {
                   var inputPhone = $('#killPhoneKey').val();
                   if(seckill.validatePhone(inputPhone)){
                       //phone写入cookie
                       $.cookie('killPhone', inputPhone, {expires:7,path:'/seckill'});
                       //刷新页面
                       window.location.reload();
                   }else{
                       $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(500);
                   }
               });
           }

           //已经登陆
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(),{},function (result) {
                if(result && result['success']){
                    var timeNow = result['data'];
                    seckill.countdown(seckillId,timeNow,startTime,endTime);
                }
            })
        }
    }
}