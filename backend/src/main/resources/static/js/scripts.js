// /*!
// * Start Bootstrap - Shop Homepage v5.0.5 (https://startbootstrap.com/template/shop-homepage)
// * Copyright 2013-2022 Start Bootstrap
// * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-shop-homepage/blob/master/LICENSE)
// */
// // This file is intentionally blank
// // Use this file to add JavaScript to your project
// /*!
// * Start Bootstrap - Shop Item v5.0.6 (https://startbootstrap.com/template/shop-item)
// * Copyright 2013-2023 Start Bootstrap
// * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-shop-item/blob/master/LICENSE)
// */
// // This file is intentionally blank
// // Use this file to add JavaScript to your project
//
// <!-- 장바구니 상품 금액 계산 -->
// function calculateItemPrice(itemId){
//
//     var count = $("#count_" + itemId).val();
//     var price = $("#price_" + itemId).val();
//
//     var totalPrice = price*count;
//     $("#resultPrice_" + itemId).html(totalPrice + '원');
// }
//
// function changeCount(itemId) {
//     <!-- 체크박스 표시되어있으면 -->
//     if ($("#cartItemCheck_" + itemId).is(":checked")) {
//         calculateTotalPrice();
//     }
//     calculateItemPrice(itemId);
// }
//
// <!-- 장바구니 상품 삭제 -->
// function deleteCartItem(itemId) {
//
//     var cartItemForm = {
//         cartItemId : itemId
//     };
//
//     $.ajax({
//         url: "/cart",
//         type: 'DELETE',
//         data: JSON.stringify(cartItemForm),
//         contentType: 'application/json',
//
//         <!-- 성공시 -->
//         success: function (result) {
//             alert("선택하신 상품이 삭제되었습니다.");
//             location.href="/cart"
//         },
//         <!-- 실패시 -->
//         error: function (jqXHR) {
//             alert(jqXHR.responseText);
//             location.href = "/cart";
//         }
//     })
// }
//
// <!-- 총 주문 금액 계산 -->
// function calculateTotalPrice() {
//     var totalPrice = 0;
//     $("input[name=cartItemCheck]:checked").each(function() {
//         var id = $(this).val();
//         var price = $("#price_" + id).val()*1;
//         var count = $("#count_" + id).val();
//         totalPrice += price*count;
//     })
//     $("#orderPrice").html(totalPrice+ '원');
// }
//
// <!-- 전체 선택, 해제 -->
// function checkAll() {
//     if ($("#allCheck").is(':checked')) {
//         $("input[name=cartItemCheck]").prop("checked", true);
//     }
//     else {
//         $("input[name=cartItemCheck]").prop("checked", false);
//     }
//     calculateTotalPrice();
// }
//
//
// <!-- 체크된 상품들 주문 -->
// function order() {
//
//
//     var list = new Array();
//
//     $("input[name=cartItemCheck]:checked").each(function() {
//         var id = $(this).val();
//         var count = $("#count_" + id).val();
//
//         var obj = new Object();
//         obj.itemId = id;
//         obj.count = count;
//
//         list.push(obj);
//     })
//
//     var dataDto = new Object();
//     dataDto.cartOrderDtoList = list;
//
//     $.ajax({
//         url: "/orders",
//         type: 'POST',
//         data: JSON.stringify(dataDto),
//         contentType: 'application/json',
//
//         <!-- 성공시 -->
//         success: function (result) {
//             alert("선택하신 상품 주문이 완료되었습니다.");
//             location.href="/"
//         },
//         <!-- 실패시 -->
//         error: function (jqXHR) {
//             alert(jqXHR.responseText);
//             location.href = "/cart";
//         }
//     })
// }
//
//
// <!-- 총 상품 금액 계산 -->
// function calculateTotalPrice(){
//
//     var quantity = $("#stockQuantity").val()*1;
//     var count = $("#count").val();
//     var price = $("#price").val();
//
//
//     <!-- 재고 부족 -->
//     if (quantity < count) {
//         alert("샹품 재고가 부족합니다. 재고:" + quantity + "개")
//         return;
//     }
//
//     var totalPrice = price*count;
//     $("#totalPrice").html(totalPrice + '원');
// }
//
// <!-- 장바구니 담기 -->
// function addCart() {
//
//     var count = $("#count").val();
//     var itemId = $("#id").val();
//     var cartForm = {
//         itemId : itemId,
//         count : count
//     };
//
//     $.ajax({
//         url: "/cart",
//         data: cartForm,
//         type: 'POST',
//         success: function(result) {
//             alert("상품을 담았습니다.");
//             location.href = "/";
//         },
//
//         error: function (jqXHR, textStatus, errorThrown) {
//             alert(jqXHR.responseText);
//             location.href="/members"
//         }
//     })
// }
//
// <!-- 단일 상품 바로 주문 -->
// function order() {
//
//
//     var itemId = $("#id").val();
//     var count = $("#count").val();
//     var cartForm = {
//         itemId : itemId,
//         count : count
//     };
//
//     $.ajax({
//         url: "/order",
//         data: cartForm,
//         type: 'POST',
//         success: function(result) {
//             alert("상품 주문이 완료되었습니다.");
//             location.href = "/";
//         },
//
//         error: function (jqXHR, textStatus, errorThrown) {
//             if (jqXHR.status === 401) {
//                 alert(jqXHR.responseText);
//                 location.href="/members"
//             }
//             else {
//                 alert(jqXHR.responseText);
//             }
//
//         }
//
//     })
// }
