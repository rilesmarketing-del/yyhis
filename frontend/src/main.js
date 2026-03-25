import { createApp } from "vue";
import {
  ElAlert,
  ElAside,
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElButton,
  ElCard,
  ElCol,
  ElCollapse,
  ElCollapseItem,
  ElContainer,
  ElDatePicker,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElDrawer,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElHeader,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElProgress,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElRow,
  ElSelect,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElTimeline,
  ElTimelineItem,
  ElTree,
  ElLoadingDirective,
} from "element-plus";
import "element-plus/dist/index.css";
import {
  Avatar,
  Box,
  Calendar,
  Clock,
  Coin,
  DataBoard,
  Document,
  DocumentCopy,
  EditPen,
  Lock,
  Memo,
  Monitor,
  OfficeBuilding,
  PieChart,
  Setting,
  Tickets,
  TrendCharts,
  User,
  Wallet,
} from "@element-plus/icons-vue";

import App from "./App.vue";
import router from "./router";
import "./styles/senior-care-mode.css";

const app = createApp(App);

const elementComponents = [
  ElAlert,
  ElAside,
  ElBreadcrumb,
  ElBreadcrumbItem,
  ElButton,
  ElCard,
  ElCol,
  ElCollapse,
  ElCollapseItem,
  ElContainer,
  ElDatePicker,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElDrawer,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElHeader,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElProgress,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElRow,
  ElSelect,
  ElSpace,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElTimeline,
  ElTimelineItem,
  ElTree,
];

const menuIcons = {
  Avatar,
  Box,
  Calendar,
  Clock,
  Coin,
  DataBoard,
  Document,
  DocumentCopy,
  EditPen,
  Lock,
  Memo,
  Monitor,
  OfficeBuilding,
  PieChart,
  Setting,
  Tickets,
  TrendCharts,
  User,
  Wallet,
};

for (const component of elementComponents) {
  app.component(component.name, component);
}

for (const [name, component] of Object.entries(menuIcons)) {
  app.component(name, component);
}

app.directive("loading", ElLoadingDirective);
app.use(router);
app.mount("#app");
