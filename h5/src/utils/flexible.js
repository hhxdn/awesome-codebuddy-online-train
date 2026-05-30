/**
 * 自适应方案：替代 amfe-flexible
 * 移动端按实际宽度计算 rem，桌面端最大按 750px 计算
 * 配合 postcss-pxtorem (rootValue: 37.5) 使用
 */
(function flexible(window, document) {
  const docEl = document.documentElement
  const MAX_WIDTH = 750 // 桌面端最大宽度
  const BASE_FONT_SIZE = 37.5 // 基准 font-size（375px 设计稿）

  function setRemUnit() {
    const width = Math.min(docEl.clientWidth || window.innerWidth, MAX_WIDTH)
    // 按比例计算：width / 375 * 37.5 = width / 10
    docEl.style.fontSize = (width / 10) + 'px'
  }

  setRemUnit()

  // resize 时重新计算
  window.addEventListener('resize', setRemUnit)
  // pageshow 时重新计算（处理浏览器前进后退缓存）
  window.addEventListener('pageshow', function (e) {
    if (e.persisted) setRemUnit()
  })
})(window, document)
