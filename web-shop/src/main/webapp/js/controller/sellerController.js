 //控制层 
app.controller('sellerController' ,function($scope,$controller   ,sellerService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		sellerService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		sellerService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		sellerService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}


	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.sellerId!=null){//如果有ID
			serviceObject=sellerService.update( $scope.entity ); //修改  
		}else{
			serviceObject=sellerService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	$scope.add = function(){
		sellerService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					// 重新查询 
		        	// $scope.reloadList();//重新加载
					location.href="shoplogin.html";
				}else{
					alert(response.message);
				}
			}		
		);	
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		sellerService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		sellerService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    $scope.find = function () {
		sellerService.find().success(
			function (response) {
				$scope.entity = response;
            }
		);
    }

    $scope.update=function () {
        sellerService.update($scope.entity).success(
        	function (response) {
                //$scope.entity = response.find();//重新加载
                location.href="/admin/seller.html"
            }
		)
    }

    //修改密码
    $scope.updatePassWord=function () {
		//存储网页传来的新密码和旧密码
        var newpassword = $scope.entity.mima.new;
        var oldpassword = $scope.entity.mima.old;
        //通过find()获取数据库中的用户密码
        sellerService.find().success(
            function (response) {
            	//存储到entity中
                $scope.entity = response;
                var password = $scope.entity.password;
                //判断输入的密码和数据库的密码是否一致
                if (password==oldpassword) {
                	//将新密码存到entity.password中并调用修改方法
                    $scope.entity.password = newpassword;
                    sellerService.update($scope.entity).success(
                        function (response) {
                        	//提示重新登录并注销用户
                        	alert("修改成功请重新登录");
                            location.href="/logout"
                        }
                    )
                }else{
                    alert("密码错误");
				}
            }
		)

	};

    //另一种修改传入数据库修改
    $scope.updatePassWordTwo = function () {
        sellerService.updatePassWordTwo($scope.entity.mima.old).success(
        	function (reponse) {
				if (response.success) {
                    alert("修改成功请重新登录");
                    location.href="/logout"
                }
                alert("密码错误");
            }
		)
    }
});
