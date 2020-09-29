const { description } = require('../../package')

module.exports = {
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#title
   */
  title: 'Jiny Framework',
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#description
   */
  description: description,

  /**
   * Extra tags to be injected to the page HTML `<head>`
   *
   * ref：https://v1.vuepress.vuejs.org/config/#head
   */
  head: [
    ['meta', { name: 'theme-color', content: '#D96565' }],
    ['meta', { name: 'apple-mobile-web-app-capable', content: 'yes' }],
    ['meta', { name: 'apple-mobile-web-app-status-bar-style', content: 'black' }],
    ['link', { rel: "apple-touch-icon", sizes: "180x180", href: "/jiny.png"}],
    ['link', { rel: "icon", type: "image/png", sizes: "32x32", href: "/jiny.png"}],
    ['link', { rel: "icon", type: "image/png", sizes: "16x16", href: "/jiny.png"}],
  ],

  /**
   * Theme configuration, here is the default theme configuration for VuePress.
   *
   * ref：https://v1.vuepress.vuejs.org/theme/default-theme-config.html
   */
  themeConfig: {
    logo: 'https://i.imgur.com/OpG00Ct.png',
    repo: '',
    editLinks: false,
    docsDir: '',
    editLinkText: '',
    lastUpdated: false,
    nav: [
      {
        text: 'Guide',
        link: '/guide/',
      },
      {
        text: 'Github',
        link: 'https://github.com/huynhminhtufu/jiny',
      }
    ],
    sidebar: {
      '/guide/': [
        {
          title: 'Essentials',
          collapsable: false,
          children: [
            '',
            'install',
            'quick-start'
          ]
        },
        {
          title: 'APIs',
          collapsable: false,
          children: [
            'apis/routes',
            'apis/httpresponse',
            'apis/context',
            'apis/middlewares',
            'apis/error-handler',
            'apis/renderer'
          ]
        },
        {
          title: 'Advance',
          collapsable: false,
          children: [
            'advance/project-structure',
            'advance/project-structure-scala',
            ['https://github.com/huynhminhtufu/jiny/tree/master/examples', 'See examples']
          ]
        },
        {
          title: 'NIO APIs',
          collapsable: false,
          children: [
            'nio-apis/',
            'nio-apis/async-helper',
            'nio-apis/thread-pool',
            'nio-apis/completablefuture',
            'nio-apis/reactor',
          ]
        },
      ]
    }
  },

  /**
   * Apply plugins，ref：https://v1.vuepress.vuejs.org/zh/plugin/
   */
  plugins: [
    '@vuepress/plugin-back-to-top',
    '@vuepress/plugin-medium-zoom',
  ],
}
