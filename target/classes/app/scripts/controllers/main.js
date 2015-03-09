'use strict';

/**
 * @ngdoc function
 * @name resourcesApp.controller:MainCtrl
 * @description # MainCtrl Controller of the resourcesApp
 */
angular.module('resourcesApp').controller('MainCtrl',
		function($scope, $resource) {
			$scope.messages = $resource('/api/history').query();
			var eb = new vertx.EventBus('http://'+window.location.host+ '/eventbus');
			eb.onopen = function() {
				eb.registerHandler('chat', function(message) {
					$scope.messages.push(message);
					$scope.$apply();
				});
			}

			$scope.sendMessage = function() {
				$scope.message.date = Date.now();
				eb.publish('chat', $scope.message);
				$scope.message.text = "";
			};
		});
