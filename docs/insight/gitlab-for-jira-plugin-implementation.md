# GitLab for Jira Cloud 插件完整实现指南

> 本文档详细介绍如何实现 GitLab for Jira Cloud 插件，包括架构设计、后端 API 实现、前端开发看板实现等完整步骤。

---

## 目录

- [一、架构概览](#一架构概览)
- [二、插件描述符配置](#二插件描述符配置)
- [三、后端 API 实现](#三后端-api-实现)
- [四、前端开发看板实现](#四前端开发看板实现)
- [五、完整实现步骤](#五完整实现步骤)
- [六、参考资源](#六参考资源)

---

## 一、架构概览

### 1.1 系统架构

```
┌────────────────────────────────────────────────────────────────┐
│                         Jira Cloud                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Atlassian Connect 应用层                                 │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  jiraDevelopmentTool 模块                           │  │  │
│  │  │  - 开发面板集成                                     │  │  │
│  │  │  - 仓库关联                                        │  │  │
│  │  │  - 分支创建                                        │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  IFrames 前端页面                                   │  │  │
│  │  │  - 仓库选择器                                      │  │  │
│  │  │  - 分支列表                                        │  │  │
│  │  │  - 提交历史                                        │  │  │
│  │  │  - MR/PR 信息                                      │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  生命周期事件                                      │  │  │
│  │  │  - installed (安装)                                │  │  │
│  │  │  - enabled (启用)                                  │  │  │
│  │  │  - disabled (禁用)                                 │  │  │
│  │  │  - uninstalled (卸载)                              │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
                          ↕ HTTPS / JWT 认证
┌────────────────────────────────────────────────────────────────┐
│                         GitLab 服务器                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Jira Connect 服务层                          │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  API Controllers (app/controllers/jira_connect/)   │  │  │
│  │  │  ├── application_controller.rb                     │  │  │
│  │  │  ├── branches_controller.rb                        │  │  │
│  │  │  ├── commits_controller.rb                         │  │  │
│  │  │  ├── merge_requests_controller.rb                  │  │  │
│  │  │  ├── pipelines_controller.rb                       │  │  │
│  │  │  ├── deployments_controller.rb                     │  │  │
│  │  │  └── installations_controller.rb                    │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  序列化器                      │  │  │
│  │  │  ├── branch_entity.rb                              │  │  │
│  │  │  ├── commit_entity.rb                              │  │  │
│  │  │  ├── merge_request_entity.rb                       │  │  │
│  │  │  ├── pipeline_entity.rb                            │  │  │
│  │  │  └── deployment_entity.rb                          │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  服务层                               │  │  │
│  │  │  ├── sync_service.rb                               │  │  │
│  │  │  ├── issue_key_extractor.rb                        │  │  │
│  │  │  └── dev_panel_push_service.rb                     │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Workers (异步任务)                                │  │  │
│  │  │  ├── jira_connect_sync_worker.rb                   │  │  │
│  │  │  └── jira_connect_event_worker.rb                  │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

### 1.2 数据流向

```
┌────────────────────────────────────────────────────────────────┐
│                     数据同步流程                                 │
└────────────────────────────────────────────────────────────────┘

1. GitLab 事件触发
   ├─ Push Event (代码提交)
   ├─ Merge Request Event (MR 创建/更新/合并)
   ├─ Pipeline Event (CI/CD 构建)
   └─ Deployment Event (部署)
          ↓
2. 事件监听器捕获
   └─ JiraConnect::EventListener
          ↓
3. 提取 Issue Key
   └─ JiraConnect::IssueKeyExtractor
      从分支名、提交信息中提取 (格式: PROJECT-123)
          ↓
4. 数据序列化
   └─ JiraConnect::Serializers
      转换为 Jira API 格式
          ↓
5. 推送到 Jira
   └─ POST /rest/dev-status/1.0/issue/detail
      或 /rest/dev-status/1.0/repository/batch
          ↓
6. Jira 开发面板显示
   └─ 用户在 Jira Issue 中查看开发信息
```

---

## 二、插件描述符配置

### 2.1 atlassian-connect.json 完整配置

```json
{
  "name": "GitLab for Jira",
  "description": "View GitLab development information in Jira issues",
  "key": "com.gitlab.jira",
  "baseUrl": "https://gitlab.com",
  "vendor": {
    "name": "GitLab",
    "url": "https://gitlab.com"
  },
  "authentication": {
    "type": "jwt"
  },
  "apiVersion": 1,
  "scopes": [
    "READ",
    "WRITE",
    "DELETE"
  ],
  "lifecycle": {
    "installed": "/jira_connect/install",
    "uninstalled": "/jira_connect/uninstall",
    "enabled": "/jira_connect/enabled",
    "disabled": "/jira_connect/disabled"
  },
  "modules": {
    "jiraDevelopmentTool": {
      "key": "gitlab-dev-tool",
      "name": {
        "value": "GitLab"
      },
      "application": {
        "value": "GitLab"
      },
      "url": "https://gitlab.com",
      "logoUrl": "https://gitlab.com/assets/favicon-72x72.png",
      "capabilities": [
        "branch",
        "commit",
        "build",
        "deployment"
      ],
      "actions": {
        "searchConnectedWorkspaces": {
          "url": "/jira_connect/workspaces/search"
        },
        "searchRepositories": {
          "url": "/jira_connect/repositories/search"
        },
        "associateRepository": {
          "url": "/jira_connect/repositories/associate"
        },
        "disassociateRepository": {
          "url": "/jira_connect/repositories/disassociate"
        },
        "createBranch": {
          "url": "/jira_connect/branches/create"
        }
      }
    },
    "generalPages": [
      {
        "url": "/jira_connect/configuration",
        "key": "gitlab-config-page",
        "location": "system.top.navigation.bar",
        "name": {
          "value": "GitLab Configuration"
        }
      }
    ]
  }
}
```

### 2.2 关键配置说明

| 配置项 | 说明 | 必需 |
|--------|------|------|
| `authentication.type` | JWT 认证方式 | ✅ |
| `baseUrl` | GitLab 实例地址 | ✅ |
| `lifecycle` | 生命周期事件回调 | ✅ |
| `jiraDevelopmentTool` | 开发工具模块配置 | ✅ |
| `capabilities` | 支持的功能类型 | ✅ |
| `actions` | 可执行的操作 | ⚠️ |

---

## 三、后端 API 实现

### 3.1 项目结构

```
app/
├── controllers/
│   └── jira_connect/
│       ├── application_controller.rb      # 基础控制器
│       ├── branches_controller.rb         # 分支信息 API
│       ├── commits_controller.rb          # 提交信息 API
│       ├── merge_requests_controller.rb   # MR 信息 API
│       ├── pipelines_controller.rb        # Pipeline 状态 API
│       ├── deployments_controller.rb      # 部署信息 API
│       ├── installations_controller.rb    # 安装管理
│       ├── workspaces_controller.rb       # 工作空间搜索
│       └── repositories_controller.rb     # 仓库关联
├── entities/
│   └── jira_connect/
│       ├── branch_entity.rb              # 分支实体
│       ├── commit_entity.rb              # 提交实体
│       ├── merge_request_entity.rb       # MR 实体
│       ├── pipeline_entity.rb            # Pipeline 实体
│       └── deployment_entity.rb          # 部署实体
├── services/
│   └── jira_connect/
│       ├── sync_service.rb               # 同步服务
│       ├── issue_key_extractor.rb        # Issue Key 提取
│       ├── dev_panel_push_service.rb     # 开发面板推送
│       └── jwt_client.rb                 # JWT 客户端
└── workers/
    └── jira_connect/
        ├── sync_worker.rb                # 同步 Worker
        └── event_worker.rb               # 事件处理 Worker
```

### 3.2 基础控制器实现

```ruby
# app/controllers/jira_connect/application_controller.rb
module JiraConnect
  class ApplicationController < ::ApplicationController
    skip_before_action :verify_authenticity_token

    # JWT 认证验证
    def verify_jwt_signature
      authorization_header = request.headers['Authorization']

      unless authorization_header.present?
        return render json: { error: 'Missing authorization header' }, status: :unauthorized
      end

      token = authorization_header.split(' ').last
      installation = find_installation(token)

      begin
        JWT.decode(
          token,
          installation.shared_secret,
          true,
          algorithm: 'HS256',
          verify_jti: true
        )
        @current_installation = installation
      rescue JWT::DecodeError => e
        render json: { error: 'Invalid JWT token' }, status: :unauthorized
      end
    end

    private

    def find_installation(token)
      # 从 token 中提取 client_key
      decoded = JWT.decode(token, nil, false)
      client_key = decoded.dig(0, 'iss')

      JiraConnect::Installation.find_by(client_key: client_key)
    end
  end
end
```

### 3.3 分支控制器实现

```ruby
# app/controllers/jira_connect/branches_controller.rb
module JiraConnect
  class BranchesController < ApplicationController
    before_action :verify_jwt_signature

    # GET /jira_connect/branches
    # 返回与 Issue Key 相关的所有分支
    def index
      branches = ::BranchesFinder.new(
        project: jira_project,
        issue_key: params[:issueKey],
        search: params[:search]
      ).execute

      render json: serialize_branches(branches)
    end

    # POST /jira_connect/branches/create
    # 为 Issue 创建新分支
    def create
      result = ::JiraConnect::CreateBranchService.new(
        project: jira_project,
        issue_key: params[:issueKey],
        branch_name: params[:branchName],
        source_branch: params[:sourceBranch]
      ).execute

      if result.success?
        render json: serialize_branch(result.payload), status: :created
      else
        render json: { error: result.message }, status: :unprocessable_entity
      end
    end

    private

    def jira_project
      @jira_project ||= JiraConnect::Project.find_by(
        jira_project_key: params[:projectId]
      )
    end

    def serialize_branches(branches)
      branches.map do |branch|
        BranchEntity.new(branch).as_json
      end
    end

    def serialize_branch(branch)
      BranchEntity.new(branch).as_json
    end
  end
end
```

### 3.4 实体序列化器实现

```ruby
# app/entities/jira_connect/branch_entity.rb
module JiraConnect
  class BranchEntity < BaseEntity
    expose :id
    expose :name
    expose :url
    expose :last_commit do |branch|
      {
        id: branch.commit.id,
        displayId: branch.commit.short_id,
        author: {
          name: branch.commit.author_name,
          avatar: branch.commit.author_avatar_url
        },
        authorTimestamp: branch.commit.authored_date,
        message: branch.commit.message,
        url: branch.commit_url
      }
    end
    expose :issue_keys do |branch|
      # 从分支名提取 Issue Key
      branch.name.scan(/[A-Z]+-\d+/).uniq
    end
  end
end
```

```ruby
# app/entities/jira_connect/commit_entity.rb
module JiraConnect
  class CommitEntity < BaseEntity
    expose :id
    expose :displayId do |commit|
      commit.short_id
    end
    expose :authorTimestamp do |commit|
      commit.authored_date
    end
    expose :url do |commit|
      commit_url(commit)
    end
    expose :author do |commit|
      {
        name: commit.author_name,
        avatar: commit.author_avatar_url
      }
    end
    expose :fileCount do |commit|
      commit.stats&.files_count || 0
    end
    expose :merge do |commit|
      commit.merge_commit?
    end
    expose :message
    expose :files do |commit|
      commit.diff_files.map do |file|
        {
          path: file.path,
          url: file_url(commit, file),
          changeType: file_change_type(file),
          linesAdded: file.additions,
          linesRemoved: file.deletions
        }
      end
    end
  end
end
```

### 3.5 同步服务实现

```ruby
# app/services/jira_connect/sync_service.rb
module JiraConnect
  class SyncService
    BATCH_SIZE = 20

    def self.sync_project(project)
      new(project).sync_project_data
    end

    def initialize(project)
      @project = project
      @installations = project.jira_installations.to_a
    end

    def sync_project_data
      return if @installations.empty?

      sync_merge_requests
      sync_commits
      sync_pipelines
      sync_deployments
    end

    private

    def sync_merge_requests
      @project.merge_requests
        .where('updated_at > ?', last_sync_at)
        .find_each(batch_size: BATCH_SIZE) do |mr|

        issue_keys = extract_issue_keys_from_mr(mr)

        issue_keys.each do |issue_key|
          push_to_jira(issue_key, :merge_requests, [mr])
        end
      end
    end

    def sync_commits
      @project.commits
        .where('authored_date > ?', last_sync_at)
        .find_each(batch_size: BATCH_SIZE) do |commit|

        issue_keys = extract_issue_keys_from_commit(commit)

        issue_keys.each do |issue_key|
          push_to_jira(issue_key, :commits, [commit])
        end
      end
    end

    def push_to_jira(issue_key, data_type, data)
      @installations.each do |installation|
        DevPanelPushService.push_update(
          installation,
          issue_key,
          data_type => data
        )
      end
    end

    def extract_issue_keys_from_mr(mr)
      keys = mr.title.scan(/[A-Z]+-\d+/).uniq
      keys + mr.source_branch.scan(/[A-Z]+-\d+/).uniq
    end

    def extract_issue_keys_from_commit(commit)
      commit.message.scan(/[A-Z]+-\d+/).uniq
    end

    def last_sync_at
      1.hour.ago
    end
  end
end
```

### 3.6 Jira API 客户端

```ruby
# app/services/jira_connect/client.rb
module JiraConnect
  class Client
    def initialize(installation)
      @installation = installation
    end

    def get(path, params = {})
      request(:get, path, params)
    end

    def post(path, body = {})
      request(:post, path, {}, body)
    end

    private

    def request(method, path, params = {}, body = nil)
      uri = URI.join(@installation.base_url, path)
      uri.query = URI.encode_www_form(params) if params.any?

      http = Net::HTTP.new(uri.host, uri.port)
      http.use_ssl = true
      http.verify_mode = OpenSSL::SSL::VERIFY_PEER

      request_class = case method
                      when :get then Net::HTTP::Get
                      when :post then Net::HTTP::Post
                      end

      req = request_class.new(uri)
      req['Authorization'] = "Bearer #{generate_jwt_token(method, uri)}"
      req['Content-Type'] = 'application/json'
      req.body = body.to_json if body

      response = http.request(req)
      handle_response(response)
    end

    def generate_jwt_token(method, uri)
      payload = {
        iss: @installation.client_key,
        iat: Time.now.to_i,
        exp: 3.minutes.from_now.to_i,
        qsh: generate_qsh(method, uri.request_uri)
      }

      JWT.encode(payload, @installation.shared_secret)
    end

    def generate_qsh(method, uri_path)
      canonical_url = uri_path.split('?').first
      query_string = create_query_string(uri_path)

      Digest::SHA256.hexdigest("#{method.upcase}&#{canonical_url}&#{query_string}")
    end

    def create_query_string(uri_path)
      query = uri_path.split('?')[1]
      return '' unless query

      params = URI.decode_www_form(query)
      params.sort_by { |k, _| k }.map { |k, v| "#{k}=#{v}" }.join('&')
    end

    def handle_response(response)
      case response
      when Net::HTTPSuccess
        JSON.parse(response.body) if response.body.present?
      else
        raise ApiError, "API error: #{response.code} #{response.message}"
      end
    end
  end
end
```

---

## 四、前端开发看板实现

### 4.1 开发面板架构

```
┌────────────────────────────────────────────────────────────────┐
│                    Jira Issue 页面                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Development Panel (开发面板)                             │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Header                                            │  │  │
│  │  │  - GitLab 仓库选择器                                │  │  │
│  │  │  - 刷新按钮                                        │  │  │
│  │  │  - 创建分支按钮                                    │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Branches (分支)                                    │  │  │
│  │  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  │  feature/PROJ-123-user-login                  │  │  │
│  │  │  │  ✓ Latest: abc1234 - "实现登录表单"          │  │  │
│  │  │  │  Author: 张三 | 2 hours ago                   │  │  │
│  │  │  └──────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Merge Requests (合并请求)                         │  │  │
│  │  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  │  !45 - 实现用户登录功能                       │  │  │
│  │  │  │  ✓ Opened by 张三 | Target: develop         │  │  │
│  │  │  │  Reviewers: 王五, 赵六 | +120 -15            │  │  │
│  │  │  │  [View in GitLab]                            │  │  │
│  │  │  └──────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Commits (提交)                                    │  │  │
│  │  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  │  abc1234 - "PROJ-123: 实现登录表单"           │  │  │
│  │  │  │  Author: 张三 | 2 hours ago                   │  │  │
│  │  │  │  [View in GitLab]                            │  │  │
│  │  │  └──────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Pipelines (构建流水线)                            │  │  │
│  │  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  │  #12345 - Pipeline for feature/PROJ-123      │  │  │
│  │  │  │  ⚙️ Running | Stage: test                    │  │  │
│  │  │  │  [View in GitLab]                            │  │  │
│  │  │  └──────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Deployments (部署)                                │  │  │
│  │  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  │  production | Status: Success                │  │  │
│  │  │  │  Deployed by 张三 | 1 hour ago               │  │  │
│  │  │  │  Environment: https://app.example.com        │  │  │
│  │  │  │  [View in GitLab]                            │  │  │
│  │  │  └──────────────────────────────────────────────┘  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

### 4.2 React 组件实现

```jsx
// app/views/jira_connect/development_panel.jsx
import React, { useState, useEffect } from 'react';
import AP from '@atlassian/connect-atlaskit';

const DevelopmentPanel = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [issueKey, setIssueKey] = useState(null);

  useEffect(() => {
    // 获取 Issue Key
    AP.getContext((context) => {
      setIssueKey(context.issueKey);
      fetchDevelopmentData(context.issueKey);
    });
  }, []);

  const fetchDevelopmentData = async (issueKey) => {
    try {
      setLoading(true);

      // 调用后端 API 获取开发数据
      const response = await fetch('/jira_connect/branches', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch development data');
      }

      const result = await response.json();
      setData(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBranch = async () => {
    try {
      const branchName = prompt('Enter branch name:', `feature/${issueKey}-branch`);

      if (!branchName) return;

      const response = await fetch('/jira_connect/branches/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          issueKey: issueKey,
          branchName: branchName,
          sourceBranch: 'main',
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to create branch');
      }

      // 刷新数据
      fetchDevelopmentData(issueKey);

      AP.alert('success', 'Branch created successfully!');
    } catch (err) {
      AP.alert('error', `Failed to create branch: ${err.message}`);
    }
  };

  const handleRefresh = () => {
    fetchDevelopmentData(issueKey);
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorMessage error={error} onRetry={handleRefresh} />;
  }

  return (
    <div className="jira-connect-development-panel">
      <PanelHeader
        onCreateBranch={handleCreateBranch}
        onRefresh={handleRefresh}
      />

      {data?.branches && data.branches.length > 0 && (
        <BranchesSection branches={data.branches} />
      )}

      {data?.mergeRequests && data.mergeRequests.length > 0 && (
        <MergeRequestsSection mergeRequests={data.mergeRequests} />
      )}

      {data?.commits && data.commits.length > 0 && (
        <CommitsSection commits={data.commits} />
      )}

      {(!data || Object.keys(data).length === 0) && (
        <EmptyState issueKey={issueKey} />
      )}
    </div>
  );
};

// 面板头部组件
const PanelHeader = ({ onCreateBranch, onRefresh }) => (
  <div className="panel-header">
    <div className="panel-title">
      <h3>GitLab Development Information</h3>
    </div>
    <div className="panel-actions">
      <button onClick={onRefresh} className="refresh-btn">
        🔄 Refresh
      </button>
      <button onClick={onCreateBranch} className="create-branch-btn">
        ➕ Create Branch
      </button>
    </div>
  </div>
);

// 分支列表组件
const BranchesSection = ({ branches }) => (
  <div className="branches-section">
    <h4>Branches ({branches.length})</h4>
    {branches.map((branch) => (
      <BranchItem key={branch.id} branch={branch} />
    ))}
  </div>
);

const BranchItem = ({ branch }) => (
  <div className="branch-item">
    <div className="branch-header">
      <span className="branch-name">🌿 {branch.name}</span>
      <a href={branch.url} target="_blank" rel="noopener noreferrer">
        View in GitLab
      </a>
    </div>
    <div className="branch-commit">
      <span className="commit-sha">{branch.last_commit.displayId}</span>
      <span className="commit-message">
        "{branch.last_commit.message}"
      </span>
    </div>
    <div className="branch-meta">
      <span className="commit-author">
        👤 {branch.last_commit.author.name}
      </span>
      <span className="commit-time">
        🕒 {formatTime(branch.last_commit.authorTimestamp)}
      </span>
    </div>
  </div>
);

// Merge Request 列表组件
const MergeRequestsSection = ({ mergeRequests }) => (
  <div className="merge-requests-section">
    <h4>Merge Requests ({mergeRequests.length})</h4>
    {mergeRequests.map((mr) => (
      <MergeRequestItem key={mr.id} mr={mr} />
    ))}
  </div>
);

const MergeRequestItem = ({ mr }) => {
  const statusIcon = {
    opened: '🟢',
    merged: '✅',
    closed: '❌',
  }[mr.status] || '⚪';

  return (
    <div className="mr-item">
      <div className="mr-header">
        <span className="mr-status">{statusIcon}</span>
        <span className="mr-title">!{mr.displayId} - {mr.title}</span>
      </div>
      <div className="mr-branches">
        <span className="source-branch">
          {mr.sourceBranch.name} → {mr.targetBranch.name}
        </span>
      </div>
      <div className="mr-meta">
        <span className="mr-author">
          👤 Opened by {mr.author.name}
        </span>
      </div>
      <div className="mr-actions">
        <a href={mr.url} target="_blank" rel="noopener noreferrer">
          View in GitLab
        </a>
      </div>
    </div>
  );
};

// 空状态组件
const EmptyState = ({ issueKey }) => (
  <div className="empty-state">
    <div className="empty-icon">📭</div>
    <h3>No development information found</h3>
    <p>
      No GitLab branches, commits, or merge requests found for issue{' '}
      <strong>{issueKey}</strong>
    </p>
  </div>
);

// 工具函数
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now - date;
  const diffMins = Math.floor(diffMs / 60000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} minutes ago`;

  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hours ago`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays} days ago`;

  return date.toLocaleDateString();
};

export default DevelopmentPanel;
```

### 4.3 CSS 样式

```css
/* app/assets/stylesheets/jira_connect/development_panel.css */

.jira-connect-development-panel {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 20px;
  background: #f4f5f7;
  border-radius: 4px;
}

/* 面板头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #dfe1e6;
}

.panel-title h3 {
  margin: 0;
  color: #172b4d;
  font-size: 18px;
  font-weight: 600;
}

.refresh-btn,
.create-branch-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 3px;
  font-size: 14px;
  cursor: pointer;
  margin-left: 8px;
}

.refresh-btn {
  background: #ebecf0;
  color: #172b4d;
}

.create-branch-btn {
  background: #0052cc;
  color: white;
}

/* 通用区块样式 */
.branches-section,
.merge-requests-section {
  background: white;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.branches-section h4,
.merge-requests-section h4 {
  margin: 0 0 12px 0;
  color: #172b4d;
  font-size: 14px;
  font-weight: 600;
}

/* 分支项 */
.branch-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f4f5f7;
  border-radius: 3px;
  border-left: 3px solid #0052cc;
}

.branch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.branch-name {
  font-weight: 600;
  color: #172b4d;
  font-size: 14px;
}

.branch-commit {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #6b778c;
  margin-bottom: 4px;
}

.branch-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6b778c;
}

/* Merge Request 项 */
.mr-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f4f5f7;
  border-radius: 3px;
  border-left: 3px solid #00875a;
}

.mr-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.mr-title {
  font-weight: 600;
  color: #172b4d;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 4px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
```

---

## 五、完整实现步骤

### 步骤 1: 创建插件骨架

```bash
# 创建插件目录结构
mkdir -p jira-gitlab-plugin/{app/controllers/jira_connect,app/entities/jira_connect,app/services/jira_connect}

# 创建 atlassian-connect.json
cat > atlassian-connect.json << 'EOF'
{
  "name": "GitLab for Jira",
  "description": "View GitLab development information in Jira",
  "key": "com.gitlab.jira",
  "baseUrl": "https://your-gitlab-instance.com",
  "vendor": {
    "name": "GitLab",
    "url": "https://gitlab.com"
  },
  "authentication": {
    "type": "jwt"
  },
  "apiVersion": 1,
  "scopes": ["READ", "WRITE", "DELETE"],
  "modules": {
    "jiraDevelopmentTool": {
      "key": "gitlab-dev-tool",
      "name": {"value": "GitLab"},
      "application": {"value": "GitLab"},
      "url": "https://your-gitlab-instance.com",
      "logoUrl": "https://gitlab.com/assets/favicon-72x72.png",
      "capabilities": ["branch", "commit", "build", "deployment"]
    }
  }
}
EOF
```

### 步骤 2: 实现后端 API 端点

按照第三节中的代码实现所有控制器、实体和服务器。

### 步骤 3: 实现前端开发面板

按照第四节中的代码实现 React 组件和样式。

### 步骤 4: 配置路由

```ruby
# config/routes.rb
namespace :jira_connect do
  # 生命周期事件
  post 'install', to: 'installations#install'
  post 'uninstall', to: 'installations#uninstall'

  # 开发面板数据
  get 'branches', to: 'branches#index'
  post 'branches', to: 'branches#create'

  # 仓库关联
  get 'repositories/search', to: 'repositories#search'
  post 'repositories/associate', to: 'repositories#associate'
end
```

### 步骤 5: 本地开发测试

```bash
# 启动本地服务器
rails server -p 3000

# 启动 ngrok 隧道
ngrok http 3000

# 更新 atlassian-connect.json 中的 baseUrl
# 使用 ngrok 提供的 HTTPS URL

# 在 Jira 中安装插件
# 1. 访问 Jira 设置 > Apps > Manage your apps
# 2. 点击 "Upload app"
# 3. 输入 atlassian-connect.json 的 URL
# 4. 点击 "Upload"
```

### 步骤 6: 部署到生产环境

```bash
# 构建前端资源
RAILS_ENV=production bundle exec rake assets:precompile

# 部署到生产服务器
cap production deploy

# 在 Jira Marketplace 发布插件
# 1. 访问 https://marketplace.atlassian.com
# 2. 登录并创建新插件
# 3. 上传插件描述符
# 4. 填写插件信息
# 5. 提交审核
```

---

## 六、参考资源

### 官方文档

- [Getting started with Connect - Atlassian Developer](https://developer.atlassian.com/cloud/jira/platform/getting-started-with-connect/)
- [Development Tool module for Connect - Atlassian Developer](https://developer.atlassian.com/cloud/jira/software/modules/development-tool/)
- [Connect app descriptor - Jira Cloud platform](https://developer.atlassian.com/cloud/jira/platform/connect-app-descriptor/)
- [Access Git Repo Commit Data via Jira REST API - Atlassian Support](https://support.atlassian.com/jira/kb/access-git-repo-commit-data-via-jira-rest-api-in-development-panel-data/)

### GitLab 文档

- [GitLab for Jira Cloud app - GitLab Documentation](https://docs.gitlab.com/integration/jira/connect-app/)
- [GitLab for Jira Cloud app administration - GitLab Documentation](https://docs.gitlab.com/administration/settings/jira_cloud_app/)

### 社区资源

- [About Jira Software modules for Connect - Atlassian Developer](https://developer.atlassian.com/cloud/jira/software/about-jira-modules/)
- [The Ultimate Modern Tech Stack for Atlassian Cloud Apps - Atlassian Community](https://community.atlassian.com/t5/App-Central-articles/The-Ultimate-Modern-Tech-Stack-for-Atlassian-Cloud-Apps/ba-p/2823989)
- [Integrate GitLab with Jira - Atlassian Support](https://support.atlassian.com/jira-cloud-administration/docs/integrate-gitlab-with-jira/)

---

## 附录

### A. API 端点列表

| 端点 | 方法 | 说明 |
|------|------|------|
| `/jira_connect/install` | POST | 插件安装回调 |
| `/jira_connect/uninstall` | POST | 插件卸载回调 |
| `/jira_connect/branches` | GET | 获取分支列表 |
| `/jira_connect/branches/create` | POST | 创建新分支 |
| `/jira_connect/commits` | GET | 获取提交列表 |
| `/jira_connect/merge_requests` | GET | 获取 MR 列表 |
| `/jira_connect/pipelines` | GET | 获取 Pipeline 列表 |
| `/jira_connect/deployments` | GET | 获取部署列表 |
| `/jira_connect/repositories/search` | GET | 搜索仓库 |
| `/jira_connect/repositories/associate` | POST | 关联仓库 |

### B. 数据模型

#### Installation 模型

```ruby
# Jira Connect 安装记录
JiraConnect::Installation
  - client_key: string (唯一标识)
  - shared_secret: string (JWT 密钥)
  - base_url: string (Jira 实例 URL)
  - installed_at: datetime
```

#### Project 模型

```ruby
# 关联的 GitLab 项目
JiraConnect::Project
  - gitlab_project_id: integer
  - jira_project_key: string
  - installation_id: foreign_key
```

### C. Issue Key 匹配规则

```ruby
# 正则表达式匹配 Jira Issue Key
ISSUE_KEY_PATTERN = /([A-Z]+-\d+)/

# 示例匹配:
# ✓ PROJ-123
# ✓ TASK-456
# ✓ STORY-789
# ✗ proj-123 (小写不匹配)
# ✗ PROJ123 (缺少连字符)
```

---

**文档版本**: 1.0
**最后更新**: 2025-01-12
**维护者**: GitLab Integration Team
