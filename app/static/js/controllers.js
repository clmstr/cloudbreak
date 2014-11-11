'use strict';

var $jq = jQuery.noConflict();

/* Controllers */
var log = log4javascript.getLogger("uluwatu-logger");
var popUpAppender = new log4javascript.PopUpAppender();
var layout = new log4javascript.PatternLayout("[%-5p] %m");
popUpAppender.setLayout(layout);

var uluwatuControllers = angular.module('uluwatuControllers', []);

uluwatuControllers.controller('uluwatuController', ['$scope', '$http', 'User', '$rootScope', '$filter',
    function ($scope, $http, User, $rootScope, $filter) {
        var orderBy = $filter('orderBy');
        $scope.user = User.get();

        var socket = io();
        socket.on('notification', handleNotification);

        $scope.errormessage = "";
        $scope.statusclass = "";
        $http.defaults.headers.common['Content-Type'] = 'application/json';

        $scope.azureTemplate = false;
        $scope.awsTemplate = true;
        $scope.azureCredential = false;
        $scope.awsCredential = true;
        $scope.lastOrderPredicate = 'name';

        $scope.statusMessage = "";
        $scope.statusclass = "";

        $scope.modifyStatusMessage = function(message, name) {
            var now = new Date();
            var date = now.toTimeString().split(" ")[0];
            if (name) {
                $scope.statusMessage = date + " " + name +  " " + message;
            } else {
                $scope.statusMessage = date + " " + message;
            }
        }

        $scope.modifyStatusClass = function(status) {
            $scope.statusclass = status;
        }

        $scope.addPanelJQueryEventListeners = function(panel) {
          addPanelJQueryEventListeners(panel);
        }

        $scope.addClusterFormJQEventListeners = function() {
          addClusterFormJQEventListeners();
        }

        $scope.addActiveClusterJQEventListeners = function() {
          addActiveClusterJQEventListeners();
        }

        $scope.addClusterListPanelJQEventListeners = function() {
           addClusterListPanelJQEventListeners();
        }

        $scope.addDatePickerPanelJQueryEventListeners = function() {
            addDatePickerPanelJQueryEventListeners();
        }

        $scope.addCrudControls = function() {
          addCrudControls();
        }

        $scope.order = function(predicate, reverse) {
          $scope.lastOrderPredicate = predicate;
          $rootScope.clusters = orderBy($rootScope.clusters, predicate, reverse);
        }

        $scope.orderByUptime = function() {
          $scope.lastOrderPredicate = 'uptime';
          $rootScope.clusters = orderBy($rootScope.clusters,
            function(element) {
                return parseInt(element.hoursUp * 60 + element.minutesUp);
            },
            false);
        }

        $scope.orderClusters = function() {
            if($scope.lastOrderPredicate == 'uptime') {
                $scope.orderByUptime();
            } else {
                $scope.order($scope.lastOrderPredicate, false);
            }
        };

        function handleNotification(data) {
          console.log(data)
          var eventType = data.eventType;
          switch(eventType) {
            case "REQUESTED":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "CREATE_IN_PROGRESS":
              handleStatusChange(data, $rootScope.error_msg.cluster_create_inprogress, "has-success");
              break;
            case "UPDATE_IN_PROGRESS":
              handleStatusChange(data, $rootScope.error_msg.cluster_update_inprogress, "has-success");
              break;
            case "CREATE_FAILED":
              handleStatusChange(data, $rootScope.error_msg.stack_create_failed, "has-error");
              break;
            case "DELETE_IN_PROGRESS":
              handleStatusChange(data, $rootScope.error_msg.stack_delete_in_progress, "has-success");
              break;
            case "STOPPED":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "START_REQUESTED":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "START_IN_PROGRESS":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "STOP_REQUESTED":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "STOP_IN_PROGRESS":
              handleStatusChange(data, eventType, "has-success");
              break;
            case "DELETE_COMPLETED":
              handleStatusChange(data, $rootScope.error_msg.stack_delete_completed, "has-success");
              $rootScope.clusters = $filter('filter')($rootScope.clusters, function(value, index) { return value.id != data.stackId;});
              break;
            case "AVAILABLE":
              handleAvailableNotification(data);
              break;
            case "UPTIME_NOTIFICATION":
              handleUptimeNotification(data);
              break;
            // default:
            //   console.log('default case.....')
          }
          $scope.$apply();

          function handleStatusChange(notification, message, statusClass){
            var actCluster = $filter('filter')($rootScope.clusters, { id: notification.stackId })[0];
            actCluster.status = notification.eventType;
            $scope.modifyStatusMessage(message, actCluster.name);
            $scope.modifyStatusClass(statusClass);
          }

          function handleAvailableNotification(notification) {
            var actCluster = $filter('filter')($rootScope.clusters, { id: notification.stackId })[0];
            var msg = notification.eventMessage;
            if (msg != null && msg != undefined) {
              actCluster.ambariServerIp = msg;
            }
            actCluster.status = notification.eventType;
            $scope.modifyStatusMessage($rootScope.error_msg.cluster_create_completed, actCluster.name);
            $scope.modifyStatusClass("has-success");
          }

          function handleUptimeNotification(notification) {
            var SECONDS_PER_MINUTE = 60;
            var MILLIS_PER_SECOND = 1000;
            var runningInMs = parseInt(notification.eventMessage);
            var minutes = ((runningInMs/ (MILLIS_PER_SECOND * SECONDS_PER_MINUTE)) % SECONDS_PER_MINUTE);
            var hours = (runningInMs / (MILLIS_PER_SECOND * SECONDS_PER_MINUTE * SECONDS_PER_MINUTE));
            var actCluster = $filter('filter')($rootScope.clusters, { id: notification.stackId })[0];
            actCluster.minutesUp = parseInt(minutes);
            actCluster.hoursUp = parseInt(hours);
          }
        }
    }
]);
