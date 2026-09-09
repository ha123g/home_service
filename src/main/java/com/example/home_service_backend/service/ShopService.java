package com.example.home_service_backend.service;

import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.constants.FileStorageConstants;
import com.example.home_service_backend.common.constants.MerchantConstants;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.dto.request.merchant.ApplicationImageRequest;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.dto.request.merchant.CreateMerchantApplicationRequest;
import com.example.home_service_backend.dto.request.merchant.MerchantServiceItemRequest;
import com.example.home_service_backend.dto.request.merchant.ReviewMerchantApplicationRequest;
import com.example.home_service_backend.dto.request.merchant.UpdateShopRequest;
import com.example.home_service_backend.entity.MerchantApplication;
import com.example.home_service_backend.entity.MerchantApplicationLocation;
import com.example.home_service_backend.entity.MerchantApplicationImage;
import com.example.home_service_backend.entity.ServiceCategory;
import com.example.home_service_backend.entity.ServiceListing;
import com.example.home_service_backend.entity.ServiceListingCategory;
import com.example.home_service_backend.entity.ServiceTag;
import com.example.home_service_backend.entity.ServiceListingTag;
import com.example.home_service_backend.entity.Shop;
import com.example.home_service_backend.entity.User;
import com.example.home_service_backend.entity.UserRole;
import com.example.home_service_backend.repository.MerchantApplicationRepository;
import com.example.home_service_backend.repository.MerchantApplicationLocationRepository;
import com.example.home_service_backend.repository.MerchantApplicationImageRepository;
import com.example.home_service_backend.repository.ServiceCategoryRepository;
import com.example.home_service_backend.repository.ServiceListingCategoryRepository;
import com.example.home_service_backend.repository.ServiceListingRepository;
import com.example.home_service_backend.repository.ServiceTagRepository;
import com.example.home_service_backend.repository.ServiceListingTagRepository;
import com.example.home_service_backend.repository.ShopRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.repository.UserRoleRepository;
import com.example.home_service_backend.repository.ShopSpecifications;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.vo.merchant.MerchantApplicationView;
import com.example.home_service_backend.vo.merchant.MerchantApplicationPageView;
import com.example.home_service_backend.vo.merchant.ApplicationImageView;
import com.example.home_service_backend.vo.merchant.ShopPageView;
import com.example.home_service_backend.vo.merchant.ShopView;
import com.example.home_service_backend.vo.merchant.ServiceListingView;
import com.example.home_service_backend.vo.merchant.ServiceCategoryTreeView;
import com.example.home_service_backend.vo.merchant.ServiceCatalogCategoryView;
import com.example.home_service_backend.vo.merchant.MerchantServiceItemView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private final ShopRepository shopRepository;
    private final MerchantApplicationRepository applicationRepository;
    private final MerchantApplicationLocationRepository locationRepository;
    private final MerchantApplicationImageRepository imageRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceListingCategoryRepository listingCategoryRepository;
    private final ServiceListingRepository listingRepository;
    private final ServiceTagRepository tagRepository;
    private final ServiceListingTagRepository listingTagRepository;
    private final CosObjectStorageService cosObjectStorageService;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public ShopService(ShopRepository shopRepository, MerchantApplicationRepository applicationRepository,
                       MerchantApplicationLocationRepository locationRepository,
                       MerchantApplicationImageRepository imageRepository,
                       UserRepository userRepository, UserRoleRepository userRoleRepository,
                       ServiceCategoryRepository categoryRepository,
                       ServiceListingCategoryRepository listingCategoryRepository,
                       ServiceListingRepository listingRepository,
                       ServiceTagRepository tagRepository,
                       ServiceListingTagRepository listingTagRepository,
                       CosObjectStorageService cosObjectStorageService,
                       ObjectMapper objectMapper) {
        this.shopRepository = shopRepository;
        this.applicationRepository = applicationRepository;
        this.locationRepository = locationRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.categoryRepository = categoryRepository;
        this.listingCategoryRepository = listingCategoryRepository;
        this.listingRepository = listingRepository;
        this.tagRepository = tagRepository;
        this.listingTagRepository = listingTagRepository;
        this.cosObjectStorageService = cosObjectStorageService;
        this.objectMapper = objectMapper;
    }

    /** 保留已有单元测试和旧调用方的构造方式。 */
    public ShopService(ShopRepository shopRepository, MerchantApplicationRepository applicationRepository,
                       MerchantApplicationLocationRepository locationRepository,
                       MerchantApplicationImageRepository imageRepository,
                       UserRepository userRepository, UserRoleRepository userRoleRepository,
                       ServiceCategoryRepository categoryRepository,
                       ServiceListingCategoryRepository listingCategoryRepository,
                       ServiceListingRepository listingRepository,
                       CosObjectStorageService cosObjectStorageService) {
        this(shopRepository, applicationRepository, locationRepository, imageRepository, userRepository,
                userRoleRepository, categoryRepository, listingCategoryRepository, listingRepository,
                null, null, cosObjectStorageService, new ObjectMapper());
    }

    @Transactional
    public MerchantApplicationView submitApplication(CreateMerchantApplicationRequest request) {
        LoginUser login = SecurityUtils.requireLoginUser();
        if (shopRepository.findByMerchantUserId(login.getId()).isPresent()) {
            throw new BusinessException("409", "当前用户已经拥有商铺");
        }
        if (applicationRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(login.getId(), MerchantConstants.APPLICATION_PENDING).isPresent()) {
            throw new BusinessException("409", "已有待审核的入驻申请");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String logoObjectKey = resolveLogoObjectKey(request.logoObjectKey(), request.images(), login.getId());
        if (request.images() != null) {
            if (request.images().size() > FileStorageConstants.MAX_MERCHANT_IMAGES) {
                throw new BusinessException("400", "入驻图片数量超出限制");
            }
            long gallery = request.images().stream().filter(image -> "OTHER".equals(image.imageType())).count();
            if (gallery > FileStorageConstants.MAX_MERCHANT_GALLERY) {
                throw new BusinessException("400", "商铺轮播图最多 8 张");
            }
            request.images().forEach(image -> requireImageKey(image, login.getId()));
        }
        MerchantApplication a = new MerchantApplication();
        a.setUserId(login.getId());
        a.setApplyNo("MA" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        a.setRealName(request.realName().trim()); a.setPhone(request.phone().trim());
        a.setShopName(request.shopName().trim()); a.setIntro(trim(request.intro()));
        a.setServiceArea(trim(request.serviceArea())); a.setServiceRadiusKm(request.serviceRadiusKm()); a.setAddressDetail(trim(request.addressDetail()));
        List<Long> categoryIds = normalizeIds(request.categoryIds());
        List<MerchantServiceItemRequest> serviceItems = normalizeServiceItems(request.serviceItems());
        if (!categoryIds.isEmpty() || !serviceItems.isEmpty()) {
            validateServiceExtensions(categoryIds, serviceItems);
            a.setServiceCategoryIds(joinIds(categoryIds));
            a.setServiceExtensions(writeServiceItems(serviceItems));
            // 新模型不再把平台 listing 当作入驻可选项；旧列仅为旧客户端保留。
            a.setServiceIds(null);
        } else {
            // 兼容旧客户端：旧字段只接受真实、在线的 listing，审批时复制为商家服务。
            if (request.serviceIds() == null || request.serviceIds().isEmpty()) throw new BusinessException("400", "至少选择一个服务分类并填写服务明细");
            List<Long> serviceIds = request.serviceIds().stream().filter(id -> id != null && id > 0).distinct().toList();
            Set<Long> onlineServiceIds = listingRepository.findAllById(serviceIds).stream()
                    .filter(item -> "ONLINE".equals(item.getStatus())).map(ServiceListing::getId).collect(Collectors.toSet());
            if (serviceIds.size() != request.serviceIds().size() || onlineServiceIds.size() != serviceIds.size()) throw new BusinessException("400", "服务项目选择无效，请重新选择");
            a.setServiceIds(serviceIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        a.setTags(normalizeTags(request.tags()));
        a.setProvince(trim(request.province())); a.setCity(trim(request.city())); a.setDistrict(trim(request.district()));
        a.setLongitude(request.longitude()); a.setLatitude(request.latitude()); a.setLogoObjectKey(logoObjectKey);
        a.setStatus(MerchantConstants.APPLICATION_PENDING); a.setAppliedAt(now); a.setCreatedAt(now); a.setUpdatedAt(now);
        try {
            a = applicationRepository.saveAndFlush(a);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("409", "已有待审核申请或申请编号重复");
        }
        MerchantApplicationLocation location = new MerchantApplicationLocation();
        location.setApplicationId(a.getId()); location.setProvider("AMAP"); location.setFormattedAddress(request.addressDetail().trim());
        location.setProvince(trim(request.province())); location.setCity(request.city().trim()); location.setDistrict(trim(request.district()));
        location.setLongitude(request.longitude()); location.setLatitude(request.latitude()); location.setCreatedTime(now); location.setUpdatedTime(now);
        locationRepository.save(location);
        Long applicationId = a.getId();
        if (request.images() != null) request.images().forEach(image -> {
            FileUploadPurpose purpose = FileUploadPurpose.fromImageType(image.imageType());
            String objectKey = cosObjectStorageService.requireOwnedKey(
                    image.objectKey(), login.getId(), Set.of(purpose)).objectKey();
            MerchantApplicationImage entity = new MerchantApplicationImage();
            entity.setApplicationId(applicationId); entity.setImageType(image.imageType()); entity.setObjectKey(objectKey);
            entity.setMimeType(trim(image.mimeType())); entity.setFileSize(image.fileSize()); entity.setSha256(trim(image.sha256()));
            entity.setStatus("UPLOADED"); entity.setCreatedTime(now); entity.setUpdatedTime(now); imageRepository.save(entity);
        });
        return toApplicationView(a, location);
    }

    @Transactional(readOnly = true)
    public MerchantApplicationView myApplication() {
        LoginUser login = SecurityUtils.requireLoginUser();
        return applicationRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(login.getId(), MerchantConstants.APPLICATION_PENDING)
                .or(() -> applicationRepository.findByUserIdOrderByCreatedAtDesc(login.getId(), PageRequest.of(0, 1)).stream().findFirst())
                .map(a -> toApplicationView(a, findLocation(a.getId()))).orElseThrow(() -> new BusinessException("404", "未找到入驻申请"));
    }

    @Transactional(readOnly = true)
    public MerchantApplicationPageView listApplications(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isBlank() && !MerchantConstants.APPLICATION_STATUSES.contains(status.trim().toUpperCase())) {
            throw new BusinessException("400", "入驻申请状态不合法");
        }
        Page<MerchantApplication> data = status == null || status.isBlank()
                ? applicationRepository.findAll(pageable)
                : applicationRepository.findByStatusOrderByCreatedAtAsc(status.trim().toUpperCase(), pageable);
        List<Long> applicationIds = data.getContent().stream().map(MerchantApplication::getId).toList();
        if (applicationIds.isEmpty()) return new MerchantApplicationPageView(List.of(), data.getTotalElements(), page, size);
        Map<Long, MerchantApplicationLocation> locations = locationRepository.findByApplicationIdIn(applicationIds).stream()
                .collect(Collectors.toMap(MerchantApplicationLocation::getApplicationId, item -> item));
        Map<Long, List<MerchantApplicationImage>> images = imageRepository
                .findByApplicationIdInAndStatusOrderById(applicationIds, "UPLOADED").stream()
                .collect(Collectors.groupingBy(MerchantApplicationImage::getApplicationId));
        List<MerchantApplicationView> items = data.getContent().stream().map(application -> {
            MerchantApplicationLocation location = locations.get(application.getId());
            if (location == null) throw new BusinessException("409", "入驻申请缺少位置数据");
            return toApplicationView(application, location, images.getOrDefault(application.getId(), List.of()));
        }).toList();
        return new MerchantApplicationPageView(items, data.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public MerchantApplicationView getApplication(Long id) {
        MerchantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("404", "入驻申请不存在"));
        return toApplicationView(application, findLocation(id));
    }

    @Transactional
    public MerchantApplicationView cancelMyApplication(Long id) {
        LoginUser login = SecurityUtils.requireLoginUser();
        MerchantApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("404", "入驻申请不存在"));
        if (!application.getUserId().equals(login.getId())) throw new BusinessException("403", "无权取消该申请");
        if (!MerchantConstants.APPLICATION_PENDING.equals(application.getStatus())) throw new BusinessException("409", "仅待审核申请可以取消");
        application.setStatus(MerchantConstants.APPLICATION_CANCELLED);
        application.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        return toApplicationView(applicationRepository.save(application), findLocation(id));
    }

    @Transactional
    public MerchantApplicationView review(Long applicationId, ReviewMerchantApplicationRequest request) {
        MerchantApplication a = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("404", "入驻申请不存在"));
        if (!MerchantConstants.APPLICATION_PENDING.equals(a.getStatus())) throw new BusinessException("409", "该申请已审核，不能重复操作");
        String decision = request.decision().trim().toUpperCase();
        if (!MerchantConstants.REVIEW_DECISIONS.contains(decision))
            throw new BusinessException("400", "审批结果只能是 APPROVED 或 REJECTED");
        LoginUser reviewer = SecurityUtils.requireLoginUser();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        a.setStatus(decision); a.setReviewerId(reviewer.getId()); a.setReviewRemark(trim(request.remark())); a.setReviewedAt(now); a.setUpdatedAt(now);
        try {
            a = applicationRepository.saveAndFlush(a);
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new BusinessException("409", "申请已被其他管理员处理，请刷新后重试");
        }
        if (MerchantConstants.APPLICATION_APPROVED.equals(decision)) {
            MerchantApplicationLocation location = findLocation(a.getId());
            User user = userRepository.findById(a.getUserId()).orElseThrow(() -> new BusinessException("404", "申请用户不存在"));
            if (shopRepository.findByMerchantUserId(user.getId()).isEmpty()) {
                Shop shop = new Shop();
                shop.setMerchantUserId(user.getId()); shop.setApplicationId(a.getId()); shop.setShopName(a.getShopName());
                shop.setShopIntro(a.getIntro()); shop.setContactPhone(a.getPhone()); shop.setServiceArea(a.getServiceArea()); shop.setServiceRadiusKm(a.getServiceRadiusKm());
                shop.setAddressDetail(location.getFormattedAddress()); shop.setProvince(location.getProvince()); shop.setCity(location.getCity()); shop.setDistrict(location.getDistrict());
                shop.setLongitude(location.getLongitude()); shop.setLatitude(location.getLatitude()); shop.setShopLogoObjectKey(a.getLogoObjectKey()); shop.setStatus(MerchantConstants.SHOP_OPEN);
                shop.setCreatedAt(now); shop.setUpdatedAt(now); shopRepository.save(shop);
                List<String> applicationTags = parseTags(a.getTags());
                if (a.getServiceExtensions() != null && !a.getServiceExtensions().isBlank()) {
                    for (MerchantServiceItemRequest item : readServiceItems(a.getServiceExtensions())) {
                        ServiceCategory category = categoryRepository.findById(item.categoryId())
                                .orElseThrow(() -> new BusinessException("400", "服务分类不存在"));
                        ServiceListing listing = new ServiceListing();
                        listing.setShopId(shop.getId()); listing.setTitle(item.title().trim());
                        listing.setSummary(trim(item.summary())); listing.setDescription(trim(item.description()));
                        listing.setPricingUnit(item.pricingUnit().trim().toUpperCase(java.util.Locale.ROOT));
                        listing.setBasePrice(item.basePrice()); listing.setDurationMinutes(item.durationMinutes());
                        listing.setStatus("ONLINE"); listing.setCreatedTime(now); listing.setUpdatedTime(now);
                        listingRepository.save(listing);
                        ServiceListingCategory copied = new ServiceListingCategory();
                        copied.setId(nextListingCategoryId()); copied.setServiceId(listing.getId()); copied.setCategoryId(category.getId());
                        listingCategoryRepository.save(copied);
                        attachTags(listing.getId(), mergeTags(applicationTags, item.tags()));
                    }
                } else if (a.getServiceIds() != null && !a.getServiceIds().isBlank()) {
                    for (String rawId : a.getServiceIds().split(",")) {
                        try {
                            Long serviceId = Long.valueOf(rawId);
                            listingRepository.findById(serviceId).ifPresent(template -> {
                                com.example.home_service_backend.entity.ServiceListing listing = new com.example.home_service_backend.entity.ServiceListing();
                                listing.setShopId(shop.getId()); listing.setTitle(template.getTitle()); listing.setSummary(template.getSummary());
                                listing.setDescription(template.getDescription()); listing.setPricingUnit(template.getPricingUnit()); listing.setBasePrice(template.getBasePrice());
                                listing.setDurationMinutes(template.getDurationMinutes()); listing.setStatus("ONLINE"); listing.setCreatedTime(now); listing.setUpdatedTime(now);
                                listingRepository.save(listing);
                                listingCategoryRepository.findByServiceIdIn(java.util.Set.of(template.getId())).forEach(link -> {
                                    com.example.home_service_backend.entity.ServiceListingCategory copied = new com.example.home_service_backend.entity.ServiceListingCategory();
                                    copied.setId(nextListingCategoryId());
                                    copied.setServiceId(listing.getId());
                                    copied.setCategoryId(link.getCategoryId());
                                 listingCategoryRepository.save(copied);
                                 });
                                 attachTags(listing.getId(), applicationTags);
                             });
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }
            if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), (long) AuthRoleEnum.USERNAME_PLA_USER.getRoleCode())) {
                UserRole role = userRoleRepository.findByUserId(user.getId()).stream()
                        .filter(item -> item.getRoleId().equals((long) AuthRoleEnum.USERNAME_NORMAL_USER.getRoleCode()))
                        .findFirst().orElseThrow(() -> new BusinessException("409", "申请用户不是可转换的平台普通用户"));
                role.setRoleId((long) AuthRoleEnum.USERNAME_PLA_USER.getRoleCode());
                userRoleRepository.save(role);
            }
        }
        return toApplicationView(a, findLocation(a.getId()));
    }

    @Transactional(readOnly = true)
    public ShopView getShop(Long id) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new BusinessException("404", "商铺不存在"));
        if (!MerchantConstants.SHOP_OPEN.equals(shop.getStatus()) && !canManage(shop)) {
            throw new BusinessException("404", "商铺不存在");
        }
        return toShop(shop);
    }

    @Transactional(readOnly = true)
    public List<ServiceListingView> listOnlineServices(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new BusinessException("404", "商铺不存在");
        }
        return listingRepository.findByShopIdAndStatusOrderByCreatedTimeDesc(shopId, "ONLINE").stream()
                .map(item -> new ServiceListingView(item.getId(), item.getShopId(), item.getTitle(),
                        item.getSummary(), item.getDescription(), item.getPricingUnit(), item.getBasePrice(),
                        item.getDurationMinutes(), item.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceListingView> listServiceCatalog() {
        LinkedHashMap<String, ServiceListingView> catalog = new LinkedHashMap<>();
        listingRepository.findByStatusOrderByTitleAsc("ONLINE").forEach(item ->
                catalog.putIfAbsent(catalogKey(item),
                        new ServiceListingView(item.getId(), item.getShopId(), item.getTitle(),
                                item.getSummary(), item.getDescription(), item.getPricingUnit(), null,
                                item.getDurationMinutes(), item.getStatus())));
        return List.copyOf(catalog.values());
    }

    @Transactional(readOnly = true)
    public List<ServiceCatalogCategoryView> serviceCatalogTree() {
        List<ServiceListing> online = listingRepository.findByStatusOrderByTitleAsc("ONLINE");
        Map<Long, ServiceListing> byId = online.stream().collect(Collectors.toMap(ServiceListing::getId, item -> item));
        Map<Long, List<Long>> serviceIdsByCategory = new LinkedHashMap<>();
        if (!byId.isEmpty()) {
            listingCategoryRepository.findByServiceIdIn(byId.keySet()).forEach(link ->
                    serviceIdsByCategory.computeIfAbsent(link.getCategoryId(), ignored -> new ArrayList<>()).add(link.getServiceId()));
        }
        Map<Long, List<ServiceCategory>> children = new LinkedHashMap<>();
        categoryRepository.findAll().forEach(item -> children.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item));
        List<ServiceCategory> rootCategories = new ArrayList<>(children.getOrDefault(null, List.of()));
        rootCategories.addAll(children.getOrDefault(0L, List.of()));
        Set<String> seen = new HashSet<>();
        return rootCategories.stream()
                .map(category -> buildCatalogCategory(category, children, serviceIdsByCategory, byId, seen,
                        new HashSet<>()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private ServiceCatalogCategoryView buildCatalogCategory(
            ServiceCategory category,
            Map<Long, List<ServiceCategory>> children,
            Map<Long, List<Long>> serviceIdsByCategory,
            Map<Long, ServiceListing> byId,
            Set<String> seen,
            Set<Long> path) {
        if (category == null || !path.add(category.getId())) return null;
        List<ServiceListingView> services = serviceIdsByCategory.getOrDefault(category.getId(), List.of()).stream()
                .map(byId::get)
                .filter(java.util.Objects::nonNull)
                .map(item -> {
                    String key = catalogKey(item);
                    return seen.add(key) ? new ServiceListingView(item.getId(), item.getShopId(), item.getTitle(),
                            item.getSummary(), item.getDescription(), item.getPricingUnit(), null,
                            item.getDurationMinutes(), item.getStatus()) : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        List<ServiceCatalogCategoryView> nested = children.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> buildCatalogCategory(child, children, serviceIdsByCategory, byId, seen,
                        new HashSet<>(path)))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (services.isEmpty() && nested.isEmpty()) return null;
        return new ServiceCatalogCategoryView(category.getId(), category.getName(), services, nested);
    }

    private String catalogKey(ServiceListing listing) {
        String title = listing.getTitle();
        return title == null ? "service:" + listing.getId() : title.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryTreeView> categoryTree() {
        List<ServiceCategory> all = categoryRepository.findAll();
        Map<Long, List<ServiceCategory>> children = new LinkedHashMap<>();
        all.forEach(item -> children.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item));
        // 标准分类是平台主数据，不依赖当前是否已有商家 listing；入驻申请必须能选择尚未被商家扩展的分类。
        List<ServiceCategoryTreeView> roots = new ArrayList<>(buildCategoryTree(null, children, new HashSet<>(), new HashSet<>()));
        roots.addAll(buildCategoryTree(0L, children, new HashSet<>(), new HashSet<>()));
        return roots;
    }

    @Transactional(readOnly = true)
    public ShopPageView search(String keyword, String province, String city, String district, String status,
                               Long categoryId, BigDecimal latitude, BigDecimal longitude, Double radiusKm, int page, int size) {
        validateSearch(latitude, longitude, radiusKm, status);
        Pageable pageable = PageRequest.of(page, size);
        Set<Long> shopIds = null;
        if (normalize(keyword) != null) {
            shopIds = new HashSet<>(listingRepository.findOnlineShopIdsByKeyword(normalize(keyword)));
            shopIds.addAll(shopRepository.findIdsByKeyword(normalize(keyword)));
            if (shopIds.isEmpty() && categoryId == null) return new ShopPageView(List.of(), 0, page, size);
        }
        if (categoryId != null) {
            Set<Long> categories = categoryTree(categoryId);
            List<Long> services = listingCategoryRepository.findServiceIdsByCategoryIds(categories);
            if (services.isEmpty()) return new ShopPageView(List.of(), 0, page, size);
            Set<Long> categoryShopIds = new HashSet<>(listingRepository.findOnlineShopIdsByIds(services));
            if (shopIds == null) shopIds = categoryShopIds; else shopIds.retainAll(categoryShopIds);
            if (shopIds.isEmpty()) return new ShopPageView(List.of(), 0, page, size);
        }
        Page<Shop> data;
        if (latitude != null && longitude != null && radiusKm != null) {
            if (radiusKm <= 0 || radiusKm > MerchantConstants.MAX_NEARBY_RADIUS_KM) throw new BusinessException("400", "距离范围应在 0 到 500 公里之间");
            boolean filterIds = shopIds != null;
            Collection<Long> queryIds = filterIds ? shopIds : List.of(-1L);
            BigDecimal latitudeDelta = BigDecimal.valueOf(radiusKm / 111.32D);
            double longitudeScale = Math.max(0.01D, Math.cos(Math.toRadians(latitude.doubleValue())));
            BigDecimal longitudeDelta = BigDecimal.valueOf(radiusKm / (111.32D * longitudeScale));
            data = shopRepository.findNearby(latitude, longitude, radiusKm,
                    normalize(status) == null ? MerchantConstants.SHOP_OPEN : status.trim().toUpperCase(),
                    shopIds == null ? normalize(keyword) : null, normalize(province), normalize(city), normalize(district),
                    filterIds, queryIds, latitude.subtract(latitudeDelta), latitude.add(latitudeDelta),
                    longitude.subtract(longitudeDelta), longitude.add(longitudeDelta), pageable);
            return new ShopPageView(data.getContent().stream()
                    .map(shop -> toShop(shop, latitude, longitude)).toList(),
                    data.getTotalElements(), page, size);
        }
        final Set<Long> ids = shopIds;
        String queryStatus = status != null && !status.isBlank() ? status.trim().toUpperCase() : MerchantConstants.SHOP_OPEN;
        data = shopRepository.findAll(ShopSpecifications.byFilters(ids == null ? keyword : null, province, city, district, queryStatus, ids),
                PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "updatedAt")));
        return new ShopPageView(data.getContent().stream().map(this::toShop).toList(), data.getTotalElements(), page, size);
    }

    @Transactional
    public ShopView updateShop(Long id, UpdateShopRequest request) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new BusinessException("404", "商铺不存在"));
        LoginUser login = SecurityUtils.requireLoginUser();
        boolean owner = shop.getMerchantUserId().equals(login.getId());
        boolean admin = isShopAdministrator();
        if (!owner && !admin) throw new BusinessException("403", "无权修改该商铺");
        if (request.shopName() != null) shop.setShopName(request.shopName().trim());
        if (request.shopLogoObjectKey() != null) {
            if (request.shopLogoObjectKey().isBlank()) {
                shop.setShopLogoObjectKey(null);
                shop.setShopLogoUrl(null);
            } else if (owner) {
                CosObjectKeyPolicy.ParsedKey key = cosObjectStorageService.requireOwnedKey(
                        request.shopLogoObjectKey(), login.getId(), Set.of(FileUploadPurpose.MERCHANT_LOGO));
                shop.setShopLogoObjectKey(key.objectKey());
                shop.setShopLogoUrl(null);
            } else {
                CosObjectKeyPolicy.ParsedKey key = CosObjectKeyPolicy.parse(
                        CosObjectKeyPolicy.normalizeSubmitted(request.shopLogoObjectKey()));
                if (key.purpose() != FileUploadPurpose.MERCHANT_LOGO) {
                    throw new BusinessException("400", "图片用途与对象 Key 不匹配");
                }
                shop.setShopLogoObjectKey(key.objectKey());
                shop.setShopLogoUrl(null);
            }
        }
        if (request.shopIntro() != null) shop.setShopIntro(request.shopIntro().trim()); if (request.contactPhone() != null) shop.setContactPhone(request.contactPhone().trim());
        if (request.serviceArea() != null) shop.setServiceArea(request.serviceArea().trim()); if (request.serviceRadiusKm() != null) shop.setServiceRadiusKm(request.serviceRadiusKm()); if (request.addressDetail() != null) shop.setAddressDetail(request.addressDetail().trim());
        if (request.province() != null) shop.setProvince(request.province().trim()); if (request.city() != null) shop.setCity(request.city().trim()); if (request.district() != null) shop.setDistrict(request.district().trim());
        if ((request.longitude() == null) != (request.latitude() == null)) throw new BusinessException("400", "经纬度必须同时修改");
        if (request.longitude() != null) shop.setLongitude(request.longitude()); if (request.latitude() != null) shop.setLatitude(request.latitude());
        shop.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); return toShop(shopRepository.save(shop));
    }

    @Transactional
    public ShopView changeStatus(Long id, String status) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new BusinessException("404", "商铺不存在"));
        LoginUser login = SecurityUtils.requireLoginUser();
        boolean owner = shop.getMerchantUserId().equals(login.getId());
        boolean admin = isShopAdministrator();
        if (!owner && !admin) throw new BusinessException("403", "无权修改该商铺状态");
        String value = status == null ? "" : status.trim().toUpperCase();
        if (!MerchantConstants.SHOP_STATUSES.contains(value)) throw new BusinessException("400", "商铺状态不合法");
        if (owner && !admin && MerchantConstants.SHOP_SUSPENDED.equals(value)) {
            throw new BusinessException("403", "商铺暂停只能由平台管理员操作");
        }
        shop.setStatus(value); shop.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC)); return toShop(shopRepository.save(shop));
    }

    private void validateSearch(BigDecimal latitude, BigDecimal longitude, Double radiusKm, String status) {
        boolean anyLocation = latitude != null || longitude != null || radiusKm != null;
        boolean allLocation = latitude != null && longitude != null && radiusKm != null;
        if (anyLocation && !allLocation) throw new BusinessException("400", "附近查询必须同时提供 latitude、longitude 和 radiusKm");
        if (latitude != null && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) throw new BusinessException("400", "纬度不合法");
        if (longitude != null && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) throw new BusinessException("400", "经度不合法");
        if (status != null && !status.isBlank() && !MerchantConstants.SHOP_STATUSES.contains(status.trim().toUpperCase())) throw new BusinessException("400", "商铺状态不合法");
        if (status != null && !status.isBlank() && !MerchantConstants.SHOP_OPEN.equals(status.trim().toUpperCase()) && !isPlatformAdmin()) throw new BusinessException("403", "无权查询非营业商铺");
    }
    private boolean canManage(Shop shop) { return shop.getMerchantUserId().equals(SecurityUtils.requireLoginUser().getId()) || isPlatformAdmin(); }
    private boolean isPlatformAdmin() { return isShopAdministrator() || hasRole(AuthRoleEnum.USERNAME_AUD_ADMIN); }
    private boolean isShopAdministrator() { return hasRole(AuthRoleEnum.USERNAME_SYS_ADMIN) || hasRole(AuthRoleEnum.USERNAME_SUPER_ADMIN); }
    private boolean hasRole(AuthRoleEnum role) { String authority = "ROLE_" + role.getValue(); return SecurityUtils.requireLoginUser().getAuthorities().stream().anyMatch(item -> authority.equals(item.getAuthority())); }
    private Set<Long> categoryTree(Long root) {
        if (!categoryRepository.existsById(root)) throw new BusinessException("404", "服务分类不存在");
        Map<Long, List<Long>> children = new LinkedHashMap<>();
        categoryRepository.findAll().forEach(item -> children.computeIfAbsent(item.getParentId(), ignored -> new ArrayList<>()).add(item.getId()));
        Set<Long> ids = new HashSet<>(); ArrayDeque<Long> queue = new ArrayDeque<>(); queue.add(root);
        while (!queue.isEmpty()) { Long id = queue.remove(); if (ids.add(id)) queue.addAll(children.getOrDefault(id, List.of())); }
        return ids;
    }
    private List<ServiceCategoryTreeView> buildCategoryTree(Long parentId, Map<Long, List<ServiceCategory>> children,
                                                              Set<Long> onlineCategoryIds, Set<Long> path) {
        return children.getOrDefault(parentId, List.of()).stream().map(item -> {
            if (!path.add(item.getId())) throw new BusinessException("409", "服务分类存在循环关系");
            List<ServiceCategoryTreeView> nested = buildCategoryTree(item.getId(), children, onlineCategoryIds, path);
            path.remove(item.getId());
            if (!onlineCategoryIds.isEmpty() && !onlineCategoryIds.contains(item.getId()) && nested.isEmpty()) return null;
            return new ServiceCategoryTreeView(item.getId(), item.getParentId(), item.getName(), nested);
        }).filter(java.util.Objects::nonNull).toList();
    }
    private String trim(String value) { return value == null ? null : value.trim(); }
    /**
     * service_listing_category 使用历史 bigint 主键且不是自增列。
     * 生成正数并检查当前库，避免 Math.abs(Long.MIN_VALUE) 产生负 ID，
     * 也避免重启/并发审批时意外覆盖已有关联。
     */
    private Long nextListingCategoryId() {
        for (int attempt = 0; attempt < 8; attempt++) {
            long candidate = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
            if (candidate == 0L) continue;
            if (!listingCategoryRepository.existsById(candidate)) return candidate;
        }
        throw new BusinessException("503", "服务分类关联暂时无法生成，请稍后重试");
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String resolveLogoObjectKey(String logoObjectKey, List<ApplicationImageRequest> images, long userId) {
        if (logoObjectKey != null && !logoObjectKey.isBlank()) {
            return cosObjectStorageService.requireOwnedKey(
                    logoObjectKey, userId, Set.of(FileUploadPurpose.MERCHANT_LOGO)).objectKey();
        }
        if (images != null) {
            return images.stream().filter(image -> "LOGO".equals(image.imageType())).findFirst()
                    .map(image -> cosObjectStorageService.requireOwnedKey(
                            image.objectKey(), userId, Set.of(FileUploadPurpose.MERCHANT_LOGO)).objectKey())
                    .orElse(null);
        }
        return null;
    }

    private void requireImageKey(ApplicationImageRequest image, long userId) {
        FileUploadPurpose purpose = FileUploadPurpose.fromImageType(image.imageType());
        CosObjectKeyPolicy.ParsedKey parsed = cosObjectStorageService.requireOwnedKey(
                image.objectKey(), userId, Set.of(purpose));
        if (parsed == null) {
            throw new BusinessException("400", "COS Object Key 不能为空");
        }
    }
    private ShopView toShop(Shop s) { return toShop(s, null, null); }
    private ShopView toShop(Shop s, BigDecimal originLatitude, BigDecimal originLongitude) {
        BigDecimal distance = null;
        if (originLatitude != null && originLongitude != null && s.getLatitude() != null && s.getLongitude() != null) {
            double lat1 = Math.toRadians(originLatitude.doubleValue());
            double lat2 = Math.toRadians(s.getLatitude().doubleValue());
            double deltaLat = lat2 - lat1;
            double deltaLon = Math.toRadians(s.getLongitude().doubleValue() - originLongitude.doubleValue());
            double a = Math.pow(Math.sin(deltaLat / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
            double normalized = Math.min(1D, Math.max(0D, a));
            distance = BigDecimal.valueOf(6371D * 2D * Math.asin(Math.sqrt(normalized)))
                    .setScale(3, java.math.RoundingMode.HALF_UP);
        }
        String logoPreview = cosObjectStorageService.previewPath(s.getShopLogoObjectKey());
        List<ServiceListing> onlineServices = listingRepository.findByShopIdAndStatusOrderByCreatedTimeDesc(s.getId(), "ONLINE");
        List<Long> serviceIds = onlineServices.stream().map(ServiceListing::getId).toList();
        List<String> serviceNames = onlineServices.stream().map(ServiceListing::getTitle)
                .filter(java.util.Objects::nonNull).distinct().toList();
        List<String> tags = tagNames(serviceIds);
        List<String> galleryUrls = imageRepository == null || s.getApplicationId() == null
                ? List.of()
                : imageRepository.findByApplicationIdAndStatusOrderById(s.getApplicationId(), "UPLOADED").stream()
                .filter(image -> !"LOGO".equals(image.getImageType()))
                .map(image -> cosObjectStorageService.previewPath(image.getObjectKey()))
                .filter(java.util.Objects::nonNull).toList();
        return new ShopView(s.getId(), s.getMerchantUserId(), s.getShopName(),
                logoPreview != null ? logoPreview : null,
                s.getShopLogoObjectKey(), s.getShopIntro(), s.getContactPhone(), s.getServiceArea(), s.getServiceRadiusKm(),
                s.getAddressDetail(), s.getProvince(), s.getCity(), s.getDistrict(), s.getLongitude(),
                s.getLatitude(), distance, s.getStatus(), s.getCreatedAt(), s.getUpdatedAt(),
                serviceNames, tags, galleryUrls);
    }
    private MerchantApplicationLocation findLocation(Long applicationId) { return locationRepository.findByApplicationId(applicationId).orElseThrow(() -> new BusinessException("409", "入驻申请缺少位置数据")); }
    private MerchantApplicationView toApplicationView(MerchantApplication a, MerchantApplicationLocation l) {
        return toApplicationView(a, l, imageRepository.findByApplicationIdAndStatusOrderById(a.getId(), "UPLOADED"));
    }
    private MerchantApplicationView toApplicationView(MerchantApplication a, MerchantApplicationLocation l,
                                                       List<MerchantApplicationImage> imageEntities) {
        List<ApplicationImageView> images = imageEntities.stream()
                .map(image -> new ApplicationImageView(image.getId(), image.getImageType(), image.getObjectKey(),
                        image.getMimeType(), image.getFileSize(), image.getSha256(),
                        cosObjectStorageService.previewPath(image.getObjectKey()))).toList();
        List<Long> selectedServiceIds = a.getServiceIds() == null || a.getServiceIds().isBlank() ? List.of() : java.util.Arrays.stream(a.getServiceIds().split(",")).map(String::trim).filter(raw -> raw.matches("\\d+")).map(Long::valueOf).toList();
        List<String> selectedServiceNames = selectedServiceIds.isEmpty() ? List.of() : listingRepository.findAllById(selectedServiceIds).stream().map(ServiceListing::getTitle).filter(java.util.Objects::nonNull).toList();
        List<MerchantServiceItemView> serviceItems = readServiceItems(a.getServiceExtensions()).stream().map(item -> {
            String categoryName = categoryRepository.findById(item.categoryId()).map(ServiceCategory::getName).orElse(null);
            return new MerchantServiceItemView(item.categoryId(), categoryName, item.title(), item.summary(), item.description(), item.pricingUnit(), item.basePrice(), item.durationMinutes(), item.tags() == null ? List.of() : item.tags());
        }).toList();
        List<Long> categoryIds = parseIds(a.getServiceCategoryIds());
        if (categoryIds.isEmpty()) categoryIds = serviceItems.stream().map(MerchantServiceItemView::categoryId).distinct().toList();
        List<String> serviceNames = serviceItems.isEmpty() ? selectedServiceNames : serviceItems.stream().map(MerchantServiceItemView::title).toList();
        return new MerchantApplicationView(a.getId(), a.getApplyNo(), a.getUserId(), a.getRealName(), a.getPhone(), a.getShopName(), a.getIntro(), a.getServiceArea(), a.getServiceRadiusKm(), l.getFormattedAddress(), l.getProvince(), l.getCity(), l.getDistrict(), l.getLongitude(), l.getLatitude(), a.getStatus(), a.getReviewerId(), a.getReviewRemark(), a.getAppliedAt(), a.getReviewedAt(), images, selectedServiceIds, serviceNames, parseTags(a.getTags()), categoryIds, serviceItems);
    }

    private List<String> tagNames(Collection<Long> serviceIds) {
        if (listingTagRepository == null || tagRepository == null || serviceIds.isEmpty()) return List.of();
        Set<Long> tagIds = listingTagRepository.findByServiceIdIn(serviceIds).stream()
                .map(ServiceListingTag::getTagId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        return tagIds.isEmpty() ? List.of() : tagRepository.findAllById(tagIds).stream()
                .map(ServiceTag::getName).filter(java.util.Objects::nonNull).distinct().toList();
    }

    private List<String> parseTags(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split(",")).map(String::trim)
                .filter(item -> !item.isBlank()).distinct().limit(12).toList();
    }

    private String normalizeTags(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream().filter(java.util.Objects::nonNull).map(String::trim)
                .filter(item -> !item.isBlank()).distinct().limit(12)
                .map(item -> item.length() > 32 ? item.substring(0, 32) : item)
                .collect(Collectors.joining(","));
    }

    private List<Long> normalizeIds(List<Long> values) {
        if (values == null) return List.of();
        return values.stream().filter(id -> id != null && id > 0).distinct().toList();
    }

    private List<Long> parseIds(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim).filter(raw -> raw.matches("\\d+"))
                .map(Long::valueOf).distinct().toList();
    }

    private String joinIds(List<Long> ids) {
        return ids.isEmpty() ? null : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<MerchantServiceItemRequest> normalizeServiceItems(List<MerchantServiceItemRequest> values) {
        if (values == null) return List.of();
        return values.stream().filter(java.util.Objects::nonNull).toList();
    }

    private void validateServiceExtensions(List<Long> categoryIds, List<MerchantServiceItemRequest> items) {
        if (categoryIds.isEmpty() || items.isEmpty()) {
            throw new BusinessException("400", "请选择服务分类并至少填写一项自定义服务");
        }
        Set<Long> existing = categoryRepository.findAllById(categoryIds).stream()
                .map(ServiceCategory::getId).collect(Collectors.toSet());
        if (existing.size() != categoryIds.size()) throw new BusinessException("400", "服务分类选择无效，请重新选择");
        Set<Long> itemCategories = items.stream().map(MerchantServiceItemRequest::categoryId).collect(Collectors.toSet());
        if (!existing.containsAll(itemCategories)) throw new BusinessException("400", "自定义服务必须属于已选择的标准分类");
    }

    private String writeServiceItems(List<MerchantServiceItemRequest> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("400", "服务明细格式无效");
        }
    }

    private List<MerchantServiceItemRequest> readServiceItems(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<MerchantServiceItemRequest>>() {});
        } catch (JsonProcessingException | RuntimeException exception) {
            throw new BusinessException("409", "入驻申请服务明细无法读取");
        }
    }

    private void attachTags(Long serviceId, List<String> names) {
        if (listingTagRepository == null || tagRepository == null || names.isEmpty()) return;
        for (String name : names) {
            ServiceTag tag = tagRepository.findByName(name).orElseGet(() -> {
                ServiceTag created = new ServiceTag();
                created.setName(name);
                created.setCreatedTime(LocalDateTime.now(ZoneOffset.UTC));
                return tagRepository.save(created);
            });
            ServiceListingTag link = new ServiceListingTag();
            link.setId(nextListingCategoryId());
            link.setServiceId(serviceId);
            link.setTagId(tag.getId());
            listingTagRepository.save(link);
        }
    }

    private List<String> mergeTags(List<String> common, List<String> specific) {
        List<String> merged = new ArrayList<>();
        if (common != null) merged.addAll(common);
        if (specific != null) merged.addAll(specific);
        return merged.stream().filter(java.util.Objects::nonNull).map(String::trim)
                .filter(item -> !item.isBlank()).distinct().limit(12).toList();
    }
}
